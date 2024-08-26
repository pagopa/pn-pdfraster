package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.service.ConvertPdfService;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.Semaphore;

import static it.pagopa.pn.pdfraster.utils.LogUtils.*;

@CustomLog
@Service
public class PdfRasterServiceImpl implements PdfRasterService {

    private final ConvertPdfService convertPdfService;

    private final Semaphore semaphore;

    public PdfRasterServiceImpl(ConvertPdfService convertPdfService,
                                @Value(value = "${pn.pdfraster.max-thread-pool-size}") Integer maxPoolSize){
        this.convertPdfService = convertPdfService;
        this.semaphore = new Semaphore(maxPoolSize);
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
