package it.pagopa.pn.pdfraster.rest.call.impl;

import it.pagopa.pn.pdfraster.rest.call.DownloadCall;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.OutputStream;

@CustomLog
@Service
public class DonwloadCallImpl implements DownloadCall {


    @Override
    public Mono<OutputStream> downloadFile(String url) {
        return null;
    }
}
