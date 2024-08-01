package it.pagopa.pn.pdfraster.rest;

import it.pagopa.pn.pdfraster.exceptions.*;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.PdfRasterResponse;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import it.pagopa.pn.pdfraster.service.SqsService;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationRequest;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationResponse;
import it.pagopa.pn.pdfraster.testutils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@SpringBootTestWebEnv
@AutoConfigureWebTestClient(timeout = "100000")
@CustomLog
public class PdfRasterApiControllerTest {

    @SpyBean
    private PdfRasterService pdfRasterService;
    @MockBean
    private SqsService sqsService;
    @MockBean
    private SafeStorageCall safeStorageCall;

    @Autowired
    private WebTestClient webTestClient;

    @Value(value = "${ss.endpoint.client-header-name}")
    private String clientId;
    private static final String CLIENT_ID = "CxId";

    @Value(value = "${ss.endpoint.api-key-header-name}")
    private String apiKey;
    private static final String X_API_KEY = "xApiKey";

    @Value(value = "${ss.endpoint.trace-id-header-name}")
    private String traceId;

    private static final String CONVERT_PDF_PATH = "/pdf-raster/convert/{filekey}";

    private static final String FILE_KEY_MOCK = "FILEKEY";

    private static final FileCreationResponse FILE_CREATION_RESPONSE = fileCreationResponseInit();
    private static final FileCreationRequest FILE_CREATION_REQUEST = fileCreationRequestInit();

    /**
     * Metodo per la creazione di un file Mock di risposta da SS
     * @return
     */
    private static FileCreationResponse fileCreationResponseInit(){
        FileCreationResponse response = new FileCreationResponse();
        response.setKey("key");
        response.setSecret("secret");
        response.setUploadUrl("url");
        response.setUploadMethod(FileCreationResponse.UploadMethodEnum.PUT);
        return response;
    }

    /**
     *
     * @return
     */
    private static FileCreationRequest fileCreationRequestInit(){
        FileCreationRequest fileCreationRequest = new FileCreationRequest();
        fileCreationRequest.setStatus("PRELOADED");
        fileCreationRequest.setContentType("application/pdf");
        fileCreationRequest.setDocumentType("PN_NOTIFICATION_ATTACHMENTS");
        return fileCreationRequest;
    }

    private @NotNull WebTestClient.ResponseSpec getPdfRasterResponseEntityExchangeResult() {
        return webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(CONVERT_PDF_PATH).build(FILE_KEY_MOCK))
                .header(clientId, CLIENT_ID)
                .header(apiKey, X_API_KEY)
                .header(traceId, "")
                .accept(APPLICATION_JSON)
                .exchange();

    }

    private void AssertionsVerifiesKo(VerificationMode sqs, VerificationMode safeStorage, EntityExchangeResult<PdfRasterResponse> responseEntity) {
        /*
            verifico che comunque safeStorage sia chiamato almeno una volta
         */
        verify(safeStorageCall, safeStorage).createFile(anyString(), anyString(), anyString(), anyString(), eq(FILE_CREATION_REQUEST));
        /*
            verifico che le code non siano mai chiamate dal momento che l'applicazione dovrebbe andare in errore prima
         */
        verify(sqsService, sqs).send(any(), any());

        /*
            Verifico che la ResponseEntity non abbia la fileKey valorizzata
         */
        Assertions.assertNotNull(responseEntity.getResponseBody());
        Assertions.assertNull(responseEntity.getResponseBody().getNewFileKey());
    }

    @Test
    void convertPdf_Ok(){
        doReturn(Mono.just(FILE_CREATION_RESPONSE)).when(safeStorageCall).createFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));
        doReturn(Mono.just(SendMessageResponse.builder().build())).when(sqsService).send(anyString(),anyString());

        EntityExchangeResult<PdfRasterResponse> responseEntity = getPdfRasterResponseEntityExchangeResult()
                .expectStatus().isOk()
                .expectBody(PdfRasterResponse.class)
                .returnResult();;

        Assertions.assertNotNull(responseEntity.getResponseBody());
        Assertions.assertNotNull(responseEntity.getResponseBody().getNewFileKey());

        verify(safeStorageCall, times(1)).createFile(any(),any(),any(),any(),any());
        verify(sqsService, times(1)).send(any(),any());
    }

    @Test
    void convertPdf_Ko_SS_Forbidden(){
        doReturn(Mono.error(new ClientNotAuthorizedException(""))).when(safeStorageCall).createFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));

        EntityExchangeResult<PdfRasterResponse> responseEntity = getPdfRasterResponseEntityExchangeResult()
                .expectStatus().isForbidden()
                .expectBody(PdfRasterResponse.class)
                .returnResult();

        AssertionsVerifiesKo(never(), times(1), responseEntity);
    }

    @Test
    void convertPdf_Ko_SS_NotFound(){
        doReturn(Mono.error(new AttachmentNotAvailableException(""))).when(safeStorageCall).createFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));

        EntityExchangeResult<PdfRasterResponse> responseEntity = getPdfRasterResponseEntityExchangeResult()
                .expectStatus().isNotFound()
                .expectBody(PdfRasterResponse.class)
                .returnResult();

        AssertionsVerifiesKo(never(), times(1), responseEntity);
    }

    @Test
    void convertPdf_Ko_SS_BadRequest(){
        doReturn(Mono.error(new Generic400ErrorException("",""))).when(safeStorageCall).createFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));

        EntityExchangeResult<PdfRasterResponse> responseEntity = getPdfRasterResponseEntityExchangeResult()
                .expectStatus().isBadRequest()
                .expectBody(PdfRasterResponse.class)
                .returnResult();

        AssertionsVerifiesKo(never(), times(1), responseEntity);
    }

    @Test
    void convertPdf_Ko_SS_InternalServerError(){
        doReturn(Mono.error(new Generic500ErrorException("",""))).when(safeStorageCall).createFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));

        EntityExchangeResult<PdfRasterResponse> responseEntity = getPdfRasterResponseEntityExchangeResult()
                .expectStatus().is5xxServerError()
                .expectBody(PdfRasterResponse.class)
                .returnResult();

        AssertionsVerifiesKo(never(), times(1), responseEntity);
    }

    @Test
    void convertPdf_Ko_Sqs(){
        doReturn(Mono.just(FILE_CREATION_RESPONSE)).when(safeStorageCall).createFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));
        doReturn(Mono.error(new SqsClientException(""))).when(sqsService).send(anyString(),anyString());

        EntityExchangeResult<PdfRasterResponse> responseEntity = getPdfRasterResponseEntityExchangeResult()
                .expectStatus().is5xxServerError()
                .expectBody(PdfRasterResponse.class)
                .returnResult();

        AssertionsVerifiesKo(times(1),times(1), responseEntity);
    }
}
