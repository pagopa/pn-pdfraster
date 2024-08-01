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

import static it.pagopa.pn.pdfraster.utils.LogUtils.POST_FILE;
import static it.pagopa.pn.pdfraster.utils.LogUtils.SAFE_STORAGE_SERVICE;

@CustomLog
@Component
public class SafeStorageCallImpl implements SafeStorageCall {

    private final WebClient ssWebClient;
    private final SafeStorageEndpointProperties safeStorageEndpointProperties;

    public SafeStorageCallImpl(WebClient webClient, SafeStorageEndpointProperties safeStorageEndpointProperties){
        this.ssWebClient = webClient;
        this.safeStorageEndpointProperties = safeStorageEndpointProperties;
    }

    @Override
    public Mono<FileCreationResponse> createFile(String xPagopaSafestorageCxId, String xApiKey, String checksumValue, String xTraceId, FileCreationRequest fileCreationRequest) {
        log.logInvokingExternalService(SAFE_STORAGE_SERVICE, POST_FILE);
        return ssWebClient.post().uri(safeStorageEndpointProperties.postFile())
                .header(safeStorageEndpointProperties.clientHeaderName(), xPagopaSafestorageCxId)
                .header(safeStorageEndpointProperties.apiKeyHeaderName(), xApiKey)
                .header(safeStorageEndpointProperties.checksumValueHeaderName(), checksumValue)
                .header(safeStorageEndpointProperties.traceIdHeaderName(), xTraceId)
                .body(BodyInserters.fromValue(fileCreationRequest))
                .retrieve()
                .bodyToMono(FileCreationResponse.class);
    }

    @Override
    public Mono<FileDownloadResponse> getFile(String fileKey, String xPagopaSafestorageCxId, String xApiKey, String xTraceId) {
        return null;
    }
}
