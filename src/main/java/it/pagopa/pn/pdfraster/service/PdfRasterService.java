package it.pagopa.pn.pdfraster.service;

import io.awspring.cloud.messaging.listener.Acknowledgment;
import it.pagopa.pn.pdfraster.safestorage.generated.openapi.server.v1.dto.TransformationMessage;
import org.springframework.core.io.ByteArrayResource;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public interface PdfRasterService {

    Mono<ByteArrayResource> convertPdf(byte[] file);
    void receiveMessage(TransformationMessage message, Acknowledgment acknowledgment);
    Mono<PutObjectResponse> processMessage(TransformationMessage messageContent);
}
