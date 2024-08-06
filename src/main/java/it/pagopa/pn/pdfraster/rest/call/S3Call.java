package it.pagopa.pn.pdfraster.rest.call;

import reactor.core.publisher.Mono;

import java.io.OutputStream;

public interface S3Call {

    Mono<OutputStream> downloadFile(String url);

}

