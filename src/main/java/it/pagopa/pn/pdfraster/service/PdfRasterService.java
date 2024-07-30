package it.pagopa.pn.pdfraster.service;

import reactor.core.publisher.Mono;

public interface PdfRasterService {

    /**
     * Metodo che fa partire il flusso di conversione
     * @param fileKey
     * @return
     */
    Mono<String> convertPdf(String fileKey);

    /**
     * Metodo che effettua la conversione di un pdf in un pdf che contiene solo images
     */
    void convertPdfToImage();
}
