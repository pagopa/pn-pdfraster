package it.pagopa.pn.pdfraster.rest.call.impl;

import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.FileCreationRequest;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.FileCreationResponse;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.FileDownloadResponse;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationRequest;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationResponse;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileDownloadResponse;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@CustomLog
@Component
public class SafeStorageCallImpl implements SafeStorageCall {


    @Override
    public Mono<FileCreationResponse> postFile(String xPagopaExtchServiceId, String xApiKey, String checksumValue, String xTraceId, FileCreationRequest fileCreationRequest) {
        return null;
    }

    @Override
    public Mono<FileDownloadResponse> getFile(String fileKey, String xPagopaExtchServiceId, String xApiKey, String xTraceId) {
        return null;
    }
}
