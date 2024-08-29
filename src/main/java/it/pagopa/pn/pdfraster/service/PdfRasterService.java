package it.pagopa.pn.pdfraster.service;

import org.springframework.core.io.ByteArrayResource;
import reactor.core.publisher.Mono;

public interface PdfRasterService {

    Mono<ByteArrayResource> convertPdf(byte[] file);
}
