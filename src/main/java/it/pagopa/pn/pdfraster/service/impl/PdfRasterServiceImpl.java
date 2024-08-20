package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.service.ConvertPdfService;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import lombok.CustomLog;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@CustomLog
@Service
public class PdfRasterServiceImpl implements PdfRasterService {

    private final ConvertPdfService convertPdfService;

    public PdfRasterServiceImpl(ConvertPdfService convertPdfService){
        this.convertPdfService = convertPdfService;
    }

    @Override
    public ByteArrayResource convertPdf(byte[] file) {
        log.info("");
        ByteArrayOutputStream convertedFile = convertPdfService.convertPdfToImage(file);
        return new ByteArrayResource(convertedFile.toByteArray());
    }
}
