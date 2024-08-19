package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.service.PdfRasterService;
import lombok.CustomLog;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@CustomLog
@Service
public class PdfRasterServiceImpl implements PdfRasterService {

    @Override
    public Resource convertPdf(byte[] file) {
        return null;
    }
}
