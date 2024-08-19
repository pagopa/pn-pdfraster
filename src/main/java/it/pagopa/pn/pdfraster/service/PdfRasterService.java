package it.pagopa.pn.pdfraster.service;

import org.springframework.core.io.Resource;

public interface PdfRasterService {

    Resource convertPdf(byte[] file);
}
