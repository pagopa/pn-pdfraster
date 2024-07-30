package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.exceptions.SqsClientException;
import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import it.pagopa.pn.pdfraster.service.SqsService;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@CustomLog
@Service
public class SqsServiceImpl implements SqsService {

    @Override
    public <T> Mono<SendMessageResponse> send(String queueName, T queuePayload) throws SqsClientException {
        return null;
    }

    @Override
    public <T> Flux<SqsMessageWrapper<T>> getMessages(String queueName, Class<T> messageContentClass) {
        return null;
    }
}
