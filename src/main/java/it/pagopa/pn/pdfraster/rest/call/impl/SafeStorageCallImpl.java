package it.pagopa.pn.pdfraster.rest.call.impl;

import it.pagopa.pn.pdfraster.configuration.properties.SafeStorageEndpointProperties;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationRequest;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationResponse;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileDownloadResponse;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@CustomLog
@Component
public class SafeStorageCallImpl implements SafeStorageCall {


    @Override
    public Mono<FileCreationResponse> postFile(String xPagopaExtchServiceId, String xApiKey, String checksumValue, String xTraceId, FileCreationRequest fileCreationRequest) {
        return null;
    public Mono<FileCreationResponse> createFile(String xPagopaSafestorageCxId, String xApiKey, String checksumValue, String xTraceId, FileCreationRequest fileCreationRequest) {
    }

    @Override
    public Mono<FileDownloadResponse> getFile(String fileKey, String xPagopaSafestorageCxId, String xApiKey, String xTraceId) {
        return null;
    }
}
