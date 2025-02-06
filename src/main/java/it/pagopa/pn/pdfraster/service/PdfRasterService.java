package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import it.pagopa.pn.pdfraster.safestorage.generated.openapi.server.v1.dto.TransformationMessage;
import org.springframework.core.io.ByteArrayResource;
import reactor.core.publisher.Mono;

public interface PdfRasterService {

    Mono<ByteArrayResource> convertPdf(byte[] file);
    void receiveMessage(TransformationMessage message);
    Mono<Void> processMessage(TransformationMessage messageContent);
}
