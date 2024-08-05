package it.pagopa.pn.pdfraster.rest.call;


import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationRequest;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationResponse;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileDownloadResponse;
import reactor.core.publisher.Mono;

public interface SafeStorageCall {

    Mono<FileCreationResponse> createFile(String xPagopaSafestorageCxId, String xApiKey, String checksumValue, String xTraceId, FileCreationRequest fileCreationRequest);

    Mono<FileDownloadResponse> getFile(String fileKey, String xPagopaSafestorageCxId, String xApiKey, String xTraceId);

}
