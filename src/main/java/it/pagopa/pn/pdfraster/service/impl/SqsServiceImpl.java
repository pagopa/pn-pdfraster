package it.pagopa.pn.pdfraster.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.pdfraster.exceptions.SqsClientException;
import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import it.pagopa.pn.pdfraster.service.SqsService;
import lombok.CustomLog;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetryBackoffSpec;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import static it.pagopa.pn.pdfraster.utils.LogUtils.*;

@CustomLog
@Service
public class SqsServiceImpl implements SqsService {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;
    private final RetryBackoffSpec sqsRetryStrategy;

    private static final int MESSAGE_GROUP_ID_LENGTH = 64;

    public SqsServiceImpl(ObjectMapper objectMapper, SqsAsyncClient sqsAsyncClient, RetryBackoffSpec sqsRetryStrategy){
        this.objectMapper = objectMapper;
        this.sqsAsyncClient = sqsAsyncClient;
        this.sqsRetryStrategy = sqsRetryStrategy;
    }

    @Override
    public <T> Mono<SendMessageResponse> send(String queueName, T queuePayload) throws SqsClientException {
        return send(queueName, (Integer) null, queuePayload);
    }

    @Override
    public <T> Mono<SendMessageResponse> send(String queueName, Integer delaySeconds, T queuePayload) throws SqsClientException {
        return send(queueName, RandomStringUtils.randomAlphanumeric(MESSAGE_GROUP_ID_LENGTH), delaySeconds, queuePayload);
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

    @Override
    public <T> Flux<SqsMessageWrapper<T>> getMessages(String queueName, Class<T> messageContentClass) {
        return null;
    }

    public Mono<String> getQueueUrlFromName(final String queueName) {
        return Mono.fromCompletionStage(sqsAsyncClient.getQueueUrl(builder -> builder.queueName(queueName)))
                .retryWhen(getSqsRetryStrategy())
                .map(GetQueueUrlResponse::queueUrl);
    }

//    @SneakyThrows
//    private <T> String writeValueAsString(T object) {
//        return objectMapper.writeValueAsString(object);
//    }

    private RetryBackoffSpec getSqsRetryStrategy() {
        var mdcContextMap = MDCUtils.retrieveMDCContextMap();
        return sqsRetryStrategy.doBeforeRetry(retrySignal -> {
            MDCUtils.enrichWithMDC(null, mdcContextMap);
            log.debug(SHORT_RETRY_ATTEMPT, retrySignal.totalRetries(), retrySignal.failure(), retrySignal.failure().getMessage());
        });
    }
}
