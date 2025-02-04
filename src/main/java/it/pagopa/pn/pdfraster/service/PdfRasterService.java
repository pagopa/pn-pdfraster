package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import it.pagopa.pn.pdfraster.model.pojo.dto.TransformationMessage;
import org.springframework.core.io.ByteArrayResource;
import reactor.core.publisher.Mono;

public interface PdfRasterService {

    Mono<ByteArrayResource> convertPdf(byte[] file);
    void receiveMessage(SqsMessageWrapper<TransformationMessage> messageWrapper);
    Mono<Void> processMessage(TransformationMessage messageContent);
}
