package it.pagopa.pn.pdfraster.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public interface ConvertPdfService {

    ByteArrayOutputStream convertPdfToImage(byte[] file);
}
