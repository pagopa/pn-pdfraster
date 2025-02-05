package it.pagopa.pn.pdfraster.service.impl;

import io.awspring.cloud.messaging.listener.annotation.SqsListener;
import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import it.pagopa.pn.pdfraster.safestorage.generated.openapi.server.v1.dto.TransformationMessage;
import it.pagopa.pn.pdfraster.service.ConvertPdfService;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.model.Tag;


import java.util.List;
import java.util.concurrent.Semaphore;

import static it.pagopa.pn.pdfraster.utils.LogUtils.*;

@CustomLog
@Service
public class PdfRasterServiceImpl implements PdfRasterService {

    private final ConvertPdfService convertPdfService;
    private final S3ServiceImpl s3Service;
    @Value("${sqs.queue.transformation-raster-queue-name}")
    private String transformationQueue;
    private static final String RASTER_TRANFORMATION_TAG = "Transformation-RASTER";
    private static final String TRANFORMATION_TAG_OK = "OK";
    private final Semaphore semaphore;


    public PdfRasterServiceImpl(ConvertPdfService convertPdfService, S3ServiceImpl s3Service,
                                @Value(value = "${pn.pdfraster.max-thread-pool-size}") Integer maxPoolSize){
        this.convertPdfService = convertPdfService;
        this.s3Service = s3Service;
        this.semaphore = new Semaphore(maxPoolSize);
    }

    @SqsListener("${sqs.queue.transformation-raster-queue-name}")
    public void receiveMessage(TransformationMessage transformationMessage) {
        log.info(INVOKING_OPERATION_LABEL, RECEIVE_MESSAGE);
        if (transformationMessage == null) {
            log.warn("Invalid message received, skipping processing.");
        } else{
            processMessage(transformationMessage)
                    .doOnError(e -> log.error("Error processing message: {}", e.getMessage()))
                    .then();
        }

    }

    @Override
    public Mono<Void> processMessage(TransformationMessage messageContent) {
        log.info(INVOKING_OPERATION_LABEL, PROCESS_MESSAGE);

        String fileKey = messageContent.getFileKey();
        String bucketName = messageContent.getBucketName();

        log.info("Start processMessage() with fileKey: {}", fileKey);

        //tag dell'oggetto per verificare se è già presente una trasformazione
        return Mono.defer(() -> s3Service.getObjectTagging(fileKey, bucketName))
                .flatMap(taggingResponse -> {
                    if (taggingResponse.hasTagSet()) {
                        List<Tag> tags = taggingResponse.tagSet();
                        //verifica se il tag di trasformazione esiste tra i tag
                        boolean hasTransformationTag = tags.stream()
                                .anyMatch(tag ->
                                        RASTER_TRANFORMATION_TAG.equals(tag.key()) && TRANFORMATION_TAG_OK.equalsIgnoreCase(tag.value()));
                        if (hasTransformationTag) {
                            log.info("File with the same transformation tag already exists, skipping processing.");
                            return Mono.empty(); //se il file ha il tag si interrompe la trasformazione
                        }
                    }
                    //se il file non ha il tag di trasformazione continuo con la trasformazione
                    return s3Service.getObject(fileKey, bucketName)
                            .flatMap(response -> convertPdfService.convertPdfToImage(response.asByteArray()))
                            .flatMap(pdfImage -> s3Service.putObject(fileKey, pdfImage.toByteArray(), messageContent.getContentType(), bucketName))
                            .then();
                });
    }


    @Override
    public Mono<ByteArrayResource> convertPdf(byte[] file) {
        log.info(INVOKING_OPERATION_LABEL,CONVERT_PDF);

        if(file.length == 0)
            throw new Generic400ErrorException(INVALID_REQUEST,"File null or empty");
        /**
         * Aquisizione del semaforo
         */
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return convertPdfService.convertPdfToImage(file)
                .map(byteArrayOutputStream -> new ByteArrayResource(byteArrayOutputStream.toByteArray()))
                .doOnSuccess(byteArrayResource -> log.info(SUCCESSFUL_OPERATION_NO_RESULT_LABEL, CONVERT_PDF))
                .doOnError(throwable -> log.error(ENDING_PROCESS_WITH_ERROR,CONVERT_PDF,throwable,throwable.getMessage()))
                .doFinally(signalType -> semaphore.release());
    }
}
