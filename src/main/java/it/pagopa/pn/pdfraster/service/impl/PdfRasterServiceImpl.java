package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.PdfRasterResponse;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import it.pagopa.pn.pdfraster.service.SqsService;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationRequest;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.UUID;

import static it.pagopa.pn.pdfraster.utils.LogUtils.*;

@CustomLog
@Service
public class PdfRasterServiceImpl implements PdfRasterService {

    private final SafeStorageCall safeStorageCall;
    private final SqsService sqsService;

    @Value(value = "${pn.pdfraster.sqs.queue.name}")
    private String queueName;

    @Value(value = "${pn.pdfraster.documentType}")
    private String documentType;

    private static final String STATUS = "PRELOADED";
    private static final String CONTENT_TYPE = "application/pdf";
    private static final String CHECKSUM_NONE = "NONE";

    public PdfRasterServiceImpl(SafeStorageCall safeStorageCall,SqsService sqsService){
        this.safeStorageCall = safeStorageCall;
        this.sqsService = sqsService;
    }

    /**
     * Metodo principale per la conversione dei pdf
     *
     * @param fileKey
     * @param xPagopaSafestorageCxId
     * @param xApiKey
     * @param xTraceId
     * @return
     */
    @Override
    public Mono<PdfRasterResponse> convertPdf(String fileKey, String xPagopaSafestorageCxId, String xApiKey, String xTraceId) {
        log.debug(INVOKING_OPERATION_LABEL_WITH_ARGS,CONVERT_PDF,fileKey);
        return safeStorageCall.createFile(xPagopaSafestorageCxId,xApiKey,CHECKSUM_NONE,checkXTraceId(xTraceId),getFileCreationRequest())
                .flatMap(fileCreationResponse -> sendToSqs(fileCreationResponse).thenReturn(fileCreationResponse))
                .map(fileCreationResponse -> {
                    String newFileKey = fileCreationResponse.getKey();
                    PdfRasterResponse response = new PdfRasterResponse();
                    response.setNewFileKey(newFileKey);
                    return response;
                })
                .doOnError(throwable -> log.info(throwable.getMessage()))
                .doOnSuccess(pdfRasterResponse -> log.info(SUCCESSFUL_OPERATION_ON_LABEL,fileKey,CONVERT_PDF,pdfRasterResponse.getNewFileKey()));

    }
    @Override
    public void convertPdfToImage() {
        // da completare
    }

    /**
     *
     * @param xTraceId
     * @return
     */
    private String checkXTraceId(String xTraceId){
        log.debug("Current Trace Id: {}", xTraceId);
        if(StringUtils.isNotBlank(xTraceId)){
            return xTraceId;
        }
        log.debug("xTraceId non valorizzato, generazione randomica");
        return UUID.randomUUID().toString();
    }

    /**
     * Metodo per creare il DTO di creazione file da inviare a SafeStorage
     */
    private FileCreationRequest getFileCreationRequest() {
        FileCreationRequest fileCreationRequest = new FileCreationRequest();
        fileCreationRequest.setStatus(STATUS);
        fileCreationRequest.setContentType(CONTENT_TYPE);
        fileCreationRequest.setDocumentType(documentType);
        return fileCreationRequest;
    }

    /**
     * Metodo per la sottoscrizione alle code SQS
     */
    private <T> Mono<SendMessageResponse> sendToSqs(T payload) {
        return sqsService.send(queueName,payload)
                .doOnSuccess(sendMessageResponse -> log.info(SUCCESSFUL_OPERATION_ON_LABEL,queueName,SQS_SEND,payload))
                .doOnError(throwable -> log.info(throwable.getMessage()));
    }
}
