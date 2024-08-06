package it.pagopa.pn.pdfraster.rest.call.impl;

import it.pagopa.pn.pdfraster.rest.call.S3Call;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.OutputStream;

@CustomLog
@Service
public class S3CallImpl implements S3Call {


    @Override
    public Mono<OutputStream> downloadFile(String url) {
        return null;
    }
}
