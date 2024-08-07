package it.pagopa.pn.pdfraster.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.pdfraster.configuration.properties.SqsRetryStrategyProperties;
import it.pagopa.pn.pdfraster.exceptions.SqsClientException;
import it.pagopa.pn.pdfraster.service.SqsService;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import static it.pagopa.pn.pdfraster.utils.LogUtils.*;

@CustomLog
@Service
public class SqsServiceImpl implements SqsService {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;
    private final RetryBackoffSpec sqsRetryStrategy;

    private static final int MESSAGE_GROUP_ID_LENGTH = 64;

    public SqsServiceImpl(ObjectMapper objectMapper, SqsAsyncClient sqsAsyncClient, SqsRetryStrategyProperties sqsRetryStrategyProperties){
        this.objectMapper = objectMapper;
        this.sqsAsyncClient = sqsAsyncClient;
        this.sqsRetryStrategy = Retry.backoff(sqsRetryStrategyProperties.maxAttempts(), Duration.ofSeconds(sqsRetryStrategyProperties.minBackoff()))
                .filter(SqsException.class::isInstance)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
    }

    @Override
    public <T> Mono<SendMessageResponse> send(String queueName, T queuePayload) throws SqsClientException {
        return send(queueName, (Integer) null, queuePayload);
    }

    @Override
    public <T> Mono<SendMessageResponse> send(String queueName, Integer delaySeconds, T queuePayload) throws SqsClientException {
        return send(queueName, generateGroupId(), delaySeconds, queuePayload);
    }

    @Override
    public <T> Mono<SendMessageResponse> send(String queueName, String messageGroupId, Integer delaySeconds, T queuePayload) throws SqsClientException {
        log.debug(INSERTING_DATA_IN_SQS, queuePayload, queueName);
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(queuePayload))
                .doOnSuccess(sendMessageResponse -> log.info("Try to publish on {} with payload {}", queueName, queuePayload))
                .zipWith(getQueueUrlFromName(queueName))
                .flatMap(objects -> Mono.fromCompletionStage(sqsAsyncClient.sendMessage(builder -> builder.queueUrl(objects.getT2())
                        .messageBody(objects.getT1())
                        .messageGroupId(messageGroupId)
                        .delaySeconds(delaySeconds))))
                .retryWhen(getSqsRetryStrategy())
                .onErrorResume(throwable -> {
                    log.error("Error on sqs publish : {}", throwable.getMessage(), throwable);
                    return Mono.error(new SqsClientException(queueName));
                })
                .doOnSuccess(result -> log.info(INSERTED_DATA_IN_SQS, queueName));
    }

    public Mono<String> getQueueUrlFromName(final String queueName) {
        return Mono.fromCompletionStage(sqsAsyncClient.getQueueUrl(builder -> builder.queueName(queueName)))
                .retryWhen(getSqsRetryStrategy())
                .map(GetQueueUrlResponse::queueUrl);
    }

    private RetryBackoffSpec getSqsRetryStrategy() {
        var mdcContextMap = MDCUtils.retrieveMDCContextMap();
        return sqsRetryStrategy.doBeforeRetry(retrySignal -> {
            MDCUtils.enrichWithMDC(null, mdcContextMap);
            log.debug(SHORT_RETRY_ATTEMPT, retrySignal.totalRetries(), retrySignal.failure(), retrySignal.failure().getMessage());
        });
    }

    private String generateGroupId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[MESSAGE_GROUP_ID_LENGTH];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }
}
