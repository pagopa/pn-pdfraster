package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.PdfRasterResponse;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationRequest;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationResponse;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@CustomLog
@Service
public class PdfRasterServiceImpl implements PdfRasterService {


    @Override
    public Mono<PdfRasterResponse> convertPdf(String fileKey) {
        return null;
    }

    @Override
    public void convertPdfToImage() {

    }
}
