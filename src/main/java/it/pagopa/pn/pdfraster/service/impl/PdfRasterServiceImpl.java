package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.pdfraster.configuration.properties.PdfRasterProperties;
import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import it.pagopa.pn.pdfraster.safestorage.generated.openapi.server.v1.dto.TransformationMessage;
import it.pagopa.pn.pdfraster.service.ConvertPdfService;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import it.pagopa.pn.pdfraster.service.SqsService;
import lombok.CustomLog;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;


import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static it.pagopa.pn.pdfraster.utils.LogUtils.*;
import static it.pagopa.pn.pdfraster.utils.SqsUtils.logIncomingMessage;

@CustomLog
@Service
public class PdfRasterServiceImpl implements PdfRasterService {
    private final ConvertPdfService convertPdfService;
    private final S3ServiceImpl s3Service;
    private final SqsService sqsService;
    private final PdfRasterProperties pdfRasterProperties;
    @Value("${sqs.queue.transformation-raster-queue-name}")
    private String transformationQueue;
    private static final String RASTER_TRANSFORMATION_TAG = "Transformation-RASTER";
    public static final String RASTER = "RASTER";
    private static final String TRANSFORMATION_TAG_OK = "OK";
    private static final String TRANSFORMATION_TAG_KO = "ERROR";
    public  static final String TRANSFORMATION_TAG_PREFIX = "Transformation-";


    public PdfRasterServiceImpl(ConvertPdfService convertPdfService, S3ServiceImpl s3Service, SqsService sqsService, PdfRasterProperties pdfRasterProperties){
        this.convertPdfService = convertPdfService;
        this.s3Service = s3Service;
        this.sqsService = sqsService;
        this.pdfRasterProperties = pdfRasterProperties;
    }


    @Scheduled(cron = "*/10 * * * * *")
    void receiveTransformationMessages() {
        log.logStartingProcess(RECEIVE_TRANSFORMATION_MESSAGES);
        AtomicBoolean hasMessages = new AtomicBoolean();
        hasMessages.set(true);
        Mono.defer(() -> sqsService.getMessages(transformationQueue, TransformationMessage.class, pdfRasterProperties.getSqs().getMaxMessages())
                        .flatMap(this::receiveMessage)
                        .collectList())
                .doOnNext(list -> hasMessages.set(!list.isEmpty()))
                .repeat(hasMessages::get)
                .doOnError(e -> log.logEndingProcess(RECEIVE_TRANSFORMATION_MESSAGES, false, e.getMessage()))
                .doOnComplete(() -> log.logEndingProcess(RECEIVE_TRANSFORMATION_MESSAGES))
                .blockLast();
    }

    public Mono<DeleteMessageResponse> receiveMessage(SqsMessageWrapper<TransformationMessage> wrapper) {
        MDCUtils.clearMDCKeys();
        TransformationMessage message = wrapper.getMessageContent();
        MDC.put(MDC_CORR_ID_KEY, message.getFileKey());
        logIncomingMessage(transformationQueue, message);
        return MDCUtils.addMDCToContextAndExecute(processMessage(message)
                .then(sqsService.deleteMessageFromQueue(wrapper.getMessage(), transformationQueue))
                .onErrorResume(e -> {
                    log.error(EXCEPTION_IN_PROCESS, RECEIVE_MESSAGE, e);
                    return Mono.empty();
                }));
    }

    @Override
    public Mono<PutObjectResponse> processMessage(TransformationMessage messageContent) {
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
                                        RASTER_TRANSFORMATION_TAG.equals(tag.key()) && TRANSFORMATION_TAG_OK.equalsIgnoreCase(tag.value()));
                        if (hasTransformationTag) {
                            log.warn("File with the same transformation tag already exists, skipping processing.");
                            return Mono.empty(); //se il file ha il tag si interrompe la trasformazione
                        }
                    }
                    //se il file non ha il tag di trasformazione continuo con la trasformazione
                    return s3Service.getObject(fileKey, bucketName)
                            .flatMap(response -> convertPdfService.convertPdfToImage(response.asByteArray()))
                            .doOnError(throwable -> {
                                        log.warn("Could not convert pdf {}, setting ERROR tag", fileKey);
                                        s3Service.putObjectTagging(fileKey, bucketName, buildTransformationTagging(RASTER, TRANSFORMATION_TAG_KO));
                                    })
                            .flatMap(pdfImage -> s3Service.putObject(fileKey, pdfImage.toByteArray(), messageContent.getContentType(), bucketName, buildTransformationTagging(RASTER, TRANSFORMATION_TAG_OK)));
                });
    }

    public static Tagging buildTransformationTagging(String transformation, String value) {
        return Tagging.builder().tagSet(Tag.builder().key(TRANSFORMATION_TAG_PREFIX + transformation).value(value).build()).build();
    }

    @Override
    public Mono<ByteArrayResource> convertPdf(byte[] file) {
        log.info(INVOKING_OPERATION_LABEL,CONVERT_PDF);

        if(file.length == 0)
            throw new Generic400ErrorException(INVALID_REQUEST,"File null or empty");

        return convertPdfService.convertPdfToImage(file)
                .map(byteArrayOutputStream -> new ByteArrayResource(byteArrayOutputStream.toByteArray()))
                .doOnSuccess(byteArrayResource -> log.info(SUCCESSFUL_OPERATION_NO_RESULT_LABEL, CONVERT_PDF))
                .doOnError(throwable -> log.error(ENDING_PROCESS_WITH_ERROR,CONVERT_PDF,throwable,throwable.getMessage()));
    }
}
