package it.pagopa.pn.pdfraster.rest;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.api.PdfRasterApi;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.PdfRasterResponse;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import lombok.CustomLog;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.pdfraster.utils.LogUtils.CONVERT_PDF;
import static it.pagopa.pn.pdfraster.utils.LogUtils.MDC_CORR_ID_KEY;

@RestController
@CustomLog
public class PdfRasterApiController implements PdfRasterApi {

    PdfRasterService pdfRasterService;

    public PdfRasterApiController(PdfRasterService pdfRasterService){
        this.pdfRasterService = pdfRasterService;
    }

    @Override
    public Mono<ResponseEntity<PdfRasterResponse>> convertPdf(String xPagopaSafestorageCxId, String xApiKey, String fileKey, String xAmznTraceId, final ServerWebExchange exchange){
        MDC.put(MDC_CORR_ID_KEY,fileKey);
        log.logStartingProcess(CONVERT_PDF);
        return MDCUtils.addMDCToContextAndExecute(pdfRasterService.convertPdf(fileKey,xPagopaSafestorageCxId,xApiKey,xAmznTraceId)
                .doOnSuccess(pdfRasterResponse -> log.logEndingProcess(CONVERT_PDF))
                .doOnError(throwable -> log.logEndingProcess(CONVERT_PDF, false, throwable.getMessage()))
        .map(ResponseEntity::ok));
    }

}
