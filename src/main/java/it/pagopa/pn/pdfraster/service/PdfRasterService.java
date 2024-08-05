package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.PdfRasterResponse;
import reactor.core.publisher.Mono;

public interface PdfRasterService {

    /**
     * Metodo che fa partire il flusso di conversione
     *
     * @param fileKey
     * @param xPagopaSafestorageCxId
     * @param xApiKey
     * @param xTraceId
     * @return
     */
    Mono<PdfRasterResponse> convertPdf(String fileKey, String xPagopaSafestorageCxId, String xApiKey, String xTraceId);

    /**
     * Metodo che effettua la conversione di un pdf in un pdf che contiene solo images
     */
    void convertPdfToImage();
}
