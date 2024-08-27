package it.pagopa.pn.pdfraster.service;

import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;

public interface ConvertPdfService {

    Mono<ByteArrayOutputStream> convertPdfToImage(byte[] file);
}
