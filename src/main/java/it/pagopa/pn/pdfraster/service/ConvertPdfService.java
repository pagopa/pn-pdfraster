package it.pagopa.pn.pdfraster.service;

import java.io.OutputStream;

public interface ConvertPdfService {

    OutputStream convertPdfToImage(byte[] file);
}
