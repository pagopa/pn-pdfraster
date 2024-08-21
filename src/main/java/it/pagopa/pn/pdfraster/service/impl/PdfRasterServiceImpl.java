package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.service.ConvertPdfService;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import it.pagopa.pn.pdfraster.utils.LogUtils;
import lombok.CustomLog;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

import static it.pagopa.pn.pdfraster.utils.LogUtils.*;

@CustomLog
@Service
public class PdfRasterServiceImpl implements PdfRasterService {

    private final ConvertPdfService convertPdfService;

    public PdfRasterServiceImpl(ConvertPdfService convertPdfService){
        this.convertPdfService = convertPdfService;
    }

    @Override
    public ByteArrayResource convertPdf(byte[] file) {
        log.debug(INVOKING_OPERATION_LABEL,CONVERT_PDF);
        ByteArrayOutputStream convertedFile = convertPdfService.convertPdfToImage(file);
        log.info(SUCCESSFUL_OPERATION_NO_RESULT_LABEL,CONVERT_PDF);
        return new ByteArrayResource(convertedFile.toByteArray());
    }
}
