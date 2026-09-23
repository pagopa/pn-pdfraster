package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import it.pagopa.pn.pdfraster.generated.openapi.msclient.safestorage.model.TransformationMessage;
import org.springframework.core.io.ByteArrayResource;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;

public interface PdfRasterService {

    Mono<ByteArrayResource> convertPdf(byte[] file);
    Mono<DeleteMessageResponse> receiveMessage(SqsMessageWrapper<TransformationMessage> wrapper);
    Mono<PutObjectResponse> processMessage(TransformationMessage messageContent);
}
