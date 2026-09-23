package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.pdfraster.configuration.properties.PnPdfRasterConfig;
import it.pagopa.pn.pdfraster.exceptions.SqsClientException;
import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import it.pagopa.pn.pdfraster.service.SqsService;
import it.pagopa.pn.pdfraster.utils.JsonUtils;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static it.pagopa.pn.pdfraster.utils.LogUtils.SHORT_RETRY_ATTEMPT;

@CustomLog
@Service
public class SqsServiceImpl implements SqsService {
    private final SqsAsyncClient sqsAsyncClient;
    private final JsonUtils jsonUtils;
    private final RetryBackoffSpec sqsRetryStrategy;

    public SqsServiceImpl(SqsAsyncClient sqsAsyncClient, JsonUtils jsonUtils, PnPdfRasterConfig config) {
        this.sqsAsyncClient = sqsAsyncClient;
        this.jsonUtils = jsonUtils;
        this.sqsRetryStrategy = Retry.backoff(config.getSqs().getRetryStrategy().getMaxAttempts(), Duration.ofSeconds(config.getSqs().getRetryStrategy().getMinBackoff()))
                .filter(SqsException.class::isInstance)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
    }

    @Override
    public <T> Mono<SendMessageResponse> send(String queueName, T queuePayload) {
        log.debug("Publishing message on SQS queue {} with payload {}", queueName, queuePayload);

        return Mono.fromCallable(() -> jsonUtils.convertObjectToJson(queuePayload))
                .zipWith(getQueueUrlFromName(queueName))
                .flatMap(tuple -> {
                    String body = tuple.getT1();
                    String queueUrl = tuple.getT2();
                    log.info("Sending message to {} with body {}", queueName, body);

                    return Mono.fromCompletionStage(
                            sqsAsyncClient.sendMessage(builder ->
                                    builder.queueUrl(queueUrl)
                                            .messageBody(body)
                            )
                    );
                })
                .retryWhen(sqsRetryStrategy)
                .doOnError(throwable -> log.error("Error publishing message to SQS {}: {}", queueName, throwable.getMessage(), throwable))
                .doOnSuccess(result -> log.info("INSERTED_DATA_IN_SQS", queueName));
    }

    @Override
    public <T> Flux<SqsMessageWrapper<T>> getMessages(String queueName, Class<T> messageContentClass, Integer maxMessages) {
        AtomicInteger actualMessages = new AtomicInteger();
        AtomicBoolean listIsEmpty = new AtomicBoolean();
        listIsEmpty.set(false);

        BooleanSupplier condition = () -> (actualMessages.get() < maxMessages && !listIsEmpty.get());

        return getQueueUrlFromName(queueName).flatMap(queueUrl -> Mono.fromCompletionStage(sqsAsyncClient.receiveMessage(builder -> builder.queueUrl(
                        queueUrl))))
                .retryWhen(getSqsRetryStrategy())
                .flatMap(receiveMessageResponse ->
                        {
                            var messages = receiveMessageResponse.messages();
                            if (messages.isEmpty())
                                listIsEmpty.set(true);
                            return Mono.justOrEmpty(messages);
                        }
                )
                .flatMapMany(Flux::fromIterable)
                .map(message ->
                {
                    actualMessages.incrementAndGet();
                    return new SqsMessageWrapper<>(message,
                            jsonUtils.convertJsonStringToObject(message.body(),
                                    messageContentClass));
                })
                .onErrorResume(throwable -> {
                    log.error(throwable.getMessage(), throwable);
                    return Mono.error(new SqsClientException(queueName));
                })
                .repeat(condition);
    }
    private RetryBackoffSpec getSqsRetryStrategy() {
        var mdcContextMap = MDCUtils.retrieveMDCContextMap();
        return sqsRetryStrategy.doBeforeRetry(retrySignal -> {
            MDCUtils.enrichWithMDC(null, mdcContextMap);
            log.info(SHORT_RETRY_ATTEMPT, retrySignal.totalRetries(), retrySignal.failure(), retrySignal.failure().getMessage());
        });
    }

    @Override
    public Mono<DeleteMessageResponse> deleteMessageFromQueue(Message message, String queueName)  {
        return getQueueUrlFromName(queueName).doOnSuccess(queueUrl -> log.debug("Delete message with id {} from {} queue",
                        message.messageId(),
                        queueName))
                .flatMap(queueUrl -> Mono.fromCompletionStage(sqsAsyncClient.deleteMessage(builder -> builder.queueUrl(
                        queueUrl).receiptHandle(message.receiptHandle()))))
                .retryWhen(getSqsRetryStrategy())
                .onErrorResume(throwable -> {
                    log.error(throwable.getMessage(), throwable);
                    return Mono.error(new SqsClientException(queueName));
                });
    }

    private Mono<String> getQueueUrlFromName(final String queueName) {
        return Mono.fromCompletionStage(sqsAsyncClient.getQueueUrl(builder -> builder.queueName(queueName)))
                .map(GetQueueUrlResponse::queueUrl);
    }
}
