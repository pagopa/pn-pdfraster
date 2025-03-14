package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;

public interface SqsService {
    <T> Flux<SqsMessageWrapper<T>> getMessages(final String queueName, final Class<T> messageContentClass, final Integer maxMessages);

    Mono<DeleteMessageResponse> deleteMessageFromQueue(final Message message, final String queueName);
}
