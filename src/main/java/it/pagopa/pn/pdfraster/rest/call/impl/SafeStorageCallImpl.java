package it.pagopa.pn.pdfraster.rest.call.impl;

import it.pagopa.pn.pdfraster.configuration.properties.SafeStorageEndpointProperties;
import it.pagopa.pn.pdfraster.exceptions.AttachmentNotAvailableException;
import it.pagopa.pn.pdfraster.exceptions.ClientNotAuthorizedException;
import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.exceptions.Generic500ErrorException;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationRequest;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationResponse;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileDownloadResponse;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

import static it.pagopa.pn.pdfraster.utils.LogUtils.POST_FILE;
import static it.pagopa.pn.pdfraster.utils.LogUtils.SAFE_STORAGE_SERVICE;

@CustomLog
@Component
public class SafeStorageCallImpl implements SafeStorageCall {

    private final WebClient ssWebClient;
    private final Retry retry;
    private final SafeStorageEndpointProperties safeStorageEndpointProperties;

    public SafeStorageCallImpl(WebClient webClient,
                               SafeStorageEndpointProperties safeStorageEndpointProperties,
                               @Value(value = "ss.retry.strategy.min-backoff") int backOff,
                               @Value(value = "ss.retry.strategy.max-attempts") int maxAttemps){
        this.ssWebClient = webClient;
        this.safeStorageEndpointProperties = safeStorageEndpointProperties;
        this.retry = Retry.backoff(maxAttemps,Duration.ofSeconds(backOff))
                .filter(Generic500ErrorException.class::isInstance)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
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
                .onStatus(HttpStatus.FORBIDDEN::equals, clientResponse -> Mono.error(new ClientNotAuthorizedException(xPagopaSafestorageCxId)))
                .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.error(new AttachmentNotAvailableException(xApiKey)))
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> Mono.error(new Generic400ErrorException(xPagopaSafestorageCxId,xApiKey)))
                .onStatus(HttpStatus.BAD_REQUEST::equals, clientResponse -> Mono.error(new Generic500ErrorException(xPagopaSafestorageCxId,xApiKey)))
                .bodyToMono(FileCreationResponse.class)
                .retryWhen(retry);
    }

    @Override
    public Mono<FileDownloadResponse> getFile(String fileKey, String xPagopaSafestorageCxId, String xApiKey, String xTraceId) {
        return null;
    }
}
