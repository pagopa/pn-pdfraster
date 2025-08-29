package it.pagopa.pn.pdfraster.service;

import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.*;

public interface S3Service {
    Mono<ResponseBytes<GetObjectResponse>> getObject(String key, String bucketName);
    Mono<PutObjectResponse> putObject(String key, byte[] fileBytes, String contentType, String bucketName);
    Mono<PutObjectResponse> putObject(String key, byte[] fileBytes, String contentType, String bucketName, Tagging tagging);
    Mono<GetObjectTaggingResponse> getObjectTagging(String key, String bucketName);
    Mono<PutObjectTaggingResponse> putObjectTagging(String key, String bucket, Tagging tagging);
}
