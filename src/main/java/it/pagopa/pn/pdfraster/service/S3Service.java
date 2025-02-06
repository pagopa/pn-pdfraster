package it.pagopa.pn.pdfraster.service;

import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public interface S3Service {
    Mono<ResponseBytes<GetObjectResponse>> getObject(String key, String bucketName);
    Mono<PutObjectResponse> putObject(String key, byte[] fileBytes, String contentType, String bucketName);
    Mono<GetObjectTaggingResponse> getObjectTagging(String key, String bucketName);

}
