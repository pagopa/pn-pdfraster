package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.service.S3Service;
import lombok.CustomLog;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.util.stream.Stream;

import static it.pagopa.pn.pdfraster.utils.LogUtils.*;


@Service
@CustomLog
public class S3ServiceImpl implements S3Service {
    private final S3AsyncClient s3AsyncClient;


    public S3ServiceImpl(S3AsyncClient s3AsyncClient) {
        this.s3AsyncClient = s3AsyncClient;

    }

    @Override
    public Mono<ResponseBytes<GetObjectResponse>> getObject(String key, String bucketName) {
        log.debug(CLIENT_METHOD_INVOCATION_WITH_ARGS, GET_OBJECT, Stream.of(key, bucketName).toList());
        return Mono.fromCompletionStage(s3AsyncClient.getObject(builder -> builder.key(key).bucket(bucketName),
                        AsyncResponseTransformer.toBytes()))
                .doOnNext(getObjectResponseResponseBytes -> log.debug(CLIENT_METHOD_RETURN, GET_OBJECT, key))
                .doOnError(e -> log.warn(CLIENT_METHOD_RETURN_WITH_ERROR, GET_OBJECT, e, e.getMessage()));
    }

    @Override
    public Mono<PutObjectResponse> putObject(String key, byte[] fileBytes, String contentType, String bucketName, Tagging tagging) {
        log.debug(CLIENT_METHOD_INVOCATION_WITH_ARGS, PUT_OBJECT, Stream.of(key, bucketName).toList());
        return Mono.fromCallable(() -> new String(Base64.encodeBase64(DigestUtils.md5(fileBytes))))
                .flatMap(contentMD5 -> Mono.fromCompletionStage(s3AsyncClient.putObject(builder -> builder.key(key)
                                .contentMD5(contentMD5)
                                .contentType(contentType)
                                .bucket(bucketName)
                                .tagging(tagging),
                        AsyncRequestBody.fromBytes(fileBytes))))
                .doOnNext(putObjectResponse -> log.info(CLIENT_METHOD_RETURN, PUT_OBJECT, putObjectResponse))
                .doOnError(throwable -> log.warn(CLIENT_METHOD_RETURN_WITH_ERROR, PUT_OBJECT, throwable, throwable.getMessage()));


    }

    @Override
    public Mono<PutObjectResponse> putObject(String key, byte[] fileBytes, String contentType, String bucketName) {
        return putObject(key, fileBytes, contentType, bucketName, null);
    }

    @Override
    public Mono<GetObjectTaggingResponse> getObjectTagging(String key, String bucketName) {
        log.debug(CLIENT_METHOD_INVOCATION_WITH_ARGS, GET_OBJECT_TAGGING, Stream.of(key, bucketName).toList());
        return Mono.fromCompletionStage(() -> s3AsyncClient.getObjectTagging(builder ->
                        builder.key(key).bucket(bucketName)))
                .doOnNext(getObjectTaggingResponse -> log.info(CLIENT_METHOD_RETURN, GET_OBJECT_TAGGING, getObjectTaggingResponse))
                .doOnError(throwable -> log.warn(CLIENT_METHOD_RETURN_WITH_ERROR, GET_OBJECT_TAGGING, throwable, throwable.getMessage()));
    }

    @Override
    public Mono<PutObjectTaggingResponse> putObjectTagging(String key, String bucketName, Tagging tagging){
        log.debug(CLIENT_METHOD_INVOCATION_WITH_ARGS, PUT_OBJECT_TAGGING, Stream.of(key, bucketName, tagging).toList());
        return Mono.fromCompletionStage(s3AsyncClient.putObjectTagging(builder -> builder.key(key).bucket(bucketName).tagging(tagging)))
                .doOnNext(putObjectTaggingResponse ->  log.info(CLIENT_METHOD_RETURN, PUT_OBJECT_TAGGING, putObjectTaggingResponse))
                .doOnError(throwable -> log.warn(CLIENT_METHOD_RETURN_WITH_ERROR, PUT_OBJECT_TAGGING, throwable, throwable.getMessage()));
    }


}
