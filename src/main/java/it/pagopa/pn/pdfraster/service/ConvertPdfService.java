package it.pagopa.pn.pdfraster.service;

import java.io.ByteArrayOutputStream;

public interface ConvertPdfService {

    ByteArrayOutputStream convertPdfToImage(byte[] file);
}
