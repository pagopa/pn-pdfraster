package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.exceptions.SqsClientException;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public interface SqsService {

    <T> Mono<SendMessageResponse> send(final String queueName, final T queuePayload) throws SqsClientException;

    <T> Mono<SendMessageResponse> send(String queueName, Integer delaySeconds, T queuePayload) throws SqsClientException;

    <T> Mono<SendMessageResponse> send(String queueName, String messageGroupId, Integer delaySeconds, T queuePayload) throws SqsClientException;
}
