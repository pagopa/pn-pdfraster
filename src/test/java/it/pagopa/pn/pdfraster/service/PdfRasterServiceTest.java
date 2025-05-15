package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.*;
import it.pagopa.pn.pdfraster.safestorage.generated.openapi.server.v1.dto.TransformationMessage;
import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import static it.pagopa.pn.pdfraster.utils.TestUtils.getFileKoTestFromResources;
import static it.pagopa.pn.pdfraster.utils.TestUtils.getFileTestFromResources;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTestWebEnv
@CustomLog
class PdfRasterServiceTest {

    @SpyBean
    private PdfRasterService pdfRasterService;

    @SpyBean
    private ConvertPdfService convertPdfService;

    @SpyBean
    private S3Service s3Service;
    @MockBean
    private SqsService sqsService;

    private static final byte[] FILE;
    private static final byte[] FILE_KO;


    static {
        FILE = getFileTestFromResources();
        FILE_KO = getFileKoTestFromResources();
    }

    private static final String FILE_KEY = "TEST.pdf";
    private static final String TRANFORMATION_RASTER_TAG = "Transformation-RASTER";
    private static final String BUCKET_NAME = "stage-bucket-test";
    private static final byte[] PDF_BYTES = {1, 2, 3};

    SqsMessageWrapper<TransformationMessage> createWrapper() {
        return createWrapper(createTransformationMessage());
    }

    SqsMessageWrapper<TransformationMessage> createWrapper(TransformationMessage transformationMessage) {
        return new SqsMessageWrapper<>(Message.builder().build(), transformationMessage);
    }

    TransformationMessage createTransformationMessage() {
        TransformationMessage transformationMessage = new TransformationMessage();
        transformationMessage.fileKey(FILE_KEY);
        transformationMessage.transformationType(TRANFORMATION_RASTER_TAG);
        transformationMessage.bucketName(BUCKET_NAME);
        transformationMessage.contentType("img/png");
        return transformationMessage;
    }


    @Test
    void testReceiveMessage_withValidMessage() {
        //WHEN
        doReturn(Mono.just(PutObjectResponse.builder().build())).when(pdfRasterService).processMessage(any(TransformationMessage.class));
        when(sqsService.deleteMessageFromQueue(any(Message.class), anyString())).thenReturn(Mono.just(DeleteMessageResponse.builder().build()));
        Mono<DeleteMessageResponse> mono = Mono.defer(() -> pdfRasterService.receiveMessage(createWrapper()));

        //THEN
        StepVerifier.create(mono).expectNextCount(1).verifyComplete();
        verify(pdfRasterService, times(1)).processMessage(any(TransformationMessage.class));
    }

    @Test
    void testReceiveMessage_withNullMessage() {
        //WHEN
        doReturn(Mono.just(PutObjectResponse.builder().build())).when(pdfRasterService).processMessage(any(TransformationMessage.class));
        Mono<DeleteMessageResponse> mono = Mono.defer(() -> pdfRasterService.receiveMessage(createWrapper(null)));

        //THEN
        StepVerifier.create(mono).expectError().verify();
        verify(pdfRasterService, never()).processMessage(null);
    }

    @Test
    void processMessageTagExists() {
        TransformationMessage message = new TransformationMessage();
        message.fileKey(FILE_KEY);
        message.bucketName(BUCKET_NAME);
        Tag tag = Tag.builder().key(TRANFORMATION_RASTER_TAG).value("OK").build();
        GetObjectTaggingResponse taggingResponse = GetObjectTaggingResponse.builder()
                .tagSet(Collections.singletonList(tag))
                .build();

        when(s3Service.getObjectTagging(FILE_KEY, BUCKET_NAME)).thenReturn(Mono.just(taggingResponse));

        Mono<PutObjectResponse> result = pdfRasterService.processMessage(message);

        StepVerifier.create(result)
                .verifyComplete();

        verify(s3Service, never()).getObject(anyString(), anyString());
        verify(convertPdfService, never()).convertPdfToImage(any());
    }

    @Test
    void processMessage_NoTagExists() {
        TransformationMessage messageContent = createTransformationMessage();
        when(s3Service.getObjectTagging(FILE_KEY, BUCKET_NAME))
                .thenReturn(Mono.just(GetObjectTaggingResponse.builder().tagSet(Collections.emptyList()).build()));

        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(
                GetObjectResponse.builder().build(), FILE);

        when(s3Service.getObject(FILE_KEY, BUCKET_NAME))
                .thenReturn(Mono.just(responseBytes));

        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        try {
            mockOutputStream.write(responseBytes.asByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        when(convertPdfService.convertPdfToImage(FILE)).thenReturn(Mono.just(mockOutputStream));

        when(s3Service.putObject(eq(FILE_KEY), any(byte[].class), eq(messageContent.getContentType()), eq(BUCKET_NAME), any(Tagging.class)))
                .thenReturn(Mono.empty());

        Mono<PutObjectResponse> result = pdfRasterService.processMessage(messageContent);

        StepVerifier.create(result)
                .verifyComplete();

        verify(s3Service, times(1)).getObjectTagging(FILE_KEY, BUCKET_NAME);
        verify(s3Service, times(1)).getObject(FILE_KEY, BUCKET_NAME);
        StepVerifier.create(convertPdfService.convertPdfToImage(FILE)).expectNextCount(1).verifyComplete();
        verify(s3Service, times(1)).putObject(eq(FILE_KEY), any(), eq(messageContent.getContentType()), eq(BUCKET_NAME), any(Tagging.class));

    }

    @Test
    void processMessage_Ko() {
        TransformationMessage messageContent = createTransformationMessage();
        when(s3Service.getObjectTagging(FILE_KEY, BUCKET_NAME)).thenReturn(Mono.error(new RuntimeException("S3 error")));

        Mono<PutObjectResponse> result = pdfRasterService.processMessage(messageContent);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(s3Service).getObjectTagging(FILE_KEY, BUCKET_NAME);
        verify(s3Service, never()).getObject(anyString(), anyString());
        verify(convertPdfService, never()).convertPdfToImage(any());
    }

    @Test
    void conversionePdf() {
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        try {
            mockOutputStream.write(PDF_BYTES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        when(convertPdfService.convertPdfToImage(any())).thenReturn(Mono.just(mockOutputStream));

        StepVerifier.create(convertPdfService.convertPdfToImage(FILE))
                .assertNext(byteArrayOutputStream -> {
                    try {
                        Assertions.assertNotNull(byteArrayOutputStream);
                        Assertions.assertTrue(byteArrayOutputStream.size() > 0);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .verifyComplete();

        verify(convertPdfService, times(1)).convertPdfToImage(any());

    }

    @Test
    void conversionePdf_KO_WrongFile() {
        when(convertPdfService.convertPdfToImage(FILE_KO))
                .thenReturn(Mono.error(new IOException("File non valido")));

        StepVerifier.create(convertPdfService.convertPdfToImage(FILE_KO))
                .expectError(IOException.class)
                .verify();

        verify(convertPdfService, times(1)).convertPdfToImage(FILE_KO);
    }

    @Test
    void conversionePdf_KO_EmptyFile() {
        when(convertPdfService.convertPdfToImage(new byte[0]))
                .thenReturn(Mono.error(new IllegalArgumentException("File vuoto non valido")));

        StepVerifier.create(convertPdfService.convertPdfToImage(new byte[0]))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(convertPdfService, times(1)).convertPdfToImage(new byte[0]);
    }

}
