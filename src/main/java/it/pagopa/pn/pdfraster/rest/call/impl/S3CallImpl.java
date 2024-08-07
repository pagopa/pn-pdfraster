package it.pagopa.pn.pdfraster.rest.call.impl;

import it.pagopa.pn.pdfraster.rest.call.S3Call;
import lombok.CustomLog;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

import static it.pagopa.pn.pdfraster.utils.LogUtils.*;

@CustomLog
@Service
public class S3CallImpl implements S3Call {

    private final WebClient s3WebClient;

    public static final String APPLICATION_PDF = "application/pdf";

    public S3CallImpl(WebClient s3WebClient){
        this.s3WebClient = s3WebClient;
    }

    @Override
    public Mono<OutputStream> downloadFile(String url) {
        OutputStream outputStream = new ByteArrayOutputStream();
        log.debug(CLIENT_METHOD_INVOCATION_WITH_ARGS, DOWNLOAD_FILE, url);
        return DataBufferUtils.write(s3WebClient.get().uri(URI.create(url)).retrieve().bodyToFlux(DataBuffer.class), outputStream)
                .map(DataBufferUtils::release)
                .then(Mono.just(outputStream))
                .doOnSuccess(result -> log.info(CLIENT_METHOD_RETURN, DOWNLOAD_FILE, url))
                .doOnError(e -> log.error("Error in downloadFile class: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> uploadFile(String url,String secret,byte[] file) {
        return s3WebClient.put()
                .uri(URI.create(url))
                .header("Content-Type", APPLICATION_PDF)
                .header("x-amz-meta-secret", secret)
                .header("x-amz-checksum-sha256", "NONE")
                .bodyValue(file)
                .retrieve()
                .toBodilessEntity()
                .then();


    }
}
