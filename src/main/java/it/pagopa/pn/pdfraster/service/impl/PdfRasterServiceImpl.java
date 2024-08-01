package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.PdfRasterResponse;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import it.pagopa.pn.pdfraster.service.SqsService;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationRequest;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.UUID;

import static it.pagopa.pn.pdfraster.utils.LogUtils.CONVERT_PDF;
import static it.pagopa.pn.pdfraster.utils.LogUtils.SQS_SEND;

@CustomLog
@Service
public class PdfRasterServiceImpl implements PdfRasterService {

    private static final String QUEUE_NAME = "pn-pdf-raster-lavorazione-queue";

    private SafeStorageCall safeStorageCall;
    private SqsService sqsService;

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
        log.debug(CONVERT_PDF);
        //TODO Gestione degli errori?
        return safeStorageCall.createFile(xPagopaSafestorageCxId,xApiKey,"NONE",checkXTraceId(xTraceId),getFileCreationRequest())
                .flatMap(fileCreationResponse -> sendToSqs(fileCreationResponse).thenReturn(fileCreationResponse))
                .map(fileCreationResponse -> {
                    String newFileKey = fileCreationResponse.getKey();
                    PdfRasterResponse response = new PdfRasterResponse();
                    response.setNewFileKey(newFileKey);
                    return response;
                })
                .doOnSuccess(pdfRasterResponse -> log.logEndingProcess(CONVERT_PDF))
                .doOnError(Exception.class, e -> log.logEndingProcess(CONVERT_PDF,false,e.getMessage()));
    }

    @Override
    public void convertPdfToImage() {

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
        fileCreationRequest.setStatus("PRELOADED");
        fileCreationRequest.setContentType("application/pdf");
        fileCreationRequest.setDocumentType("PN_NOTIFICATION_ATTACHMENTS");
        return fileCreationRequest;
    }

    /**
     * Metodo per la sottoscrizione alle code SQS
     */
    private <T> Mono<SendMessageResponse> sendToSqs(T payload) {
        return sqsService.send(QUEUE_NAME,payload) //TODO Qual'è il payload da sottoscrivere (Tutta la richiesta?)
                .doOnSuccess(sendMessageResponse -> log.logEndingProcess(SQS_SEND))
                .doOnError(throwable -> log.logEndingProcess(SQS_SEND,false,throwable.getMessage()));
    }
}
