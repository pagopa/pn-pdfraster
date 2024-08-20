package it.pagopa.pn.pdfraster.service;

import org.springframework.core.io.ByteArrayResource;

public interface PdfRasterService {

    ByteArrayResource convertPdf(byte[] file);
}
