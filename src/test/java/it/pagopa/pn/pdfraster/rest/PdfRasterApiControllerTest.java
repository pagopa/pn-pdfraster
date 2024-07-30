package it.pagopa.pn.pdfraster.rest;

import it.pagopa.pn.pdfraster.exceptions.AttachmentNotAvailableException;
import it.pagopa.pn.pdfraster.exceptions.ClientNotAuthorizedException;
import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.exceptions.Generic500ErrorException;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.FileCreationRequest;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.FileCreationResponse;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.PdfRasterResponse;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import it.pagopa.pn.pdfraster.service.SqsService;
import it.pagopa.pn.pdfraster.testutils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTestWebEnv
@AutoConfigureWebTestClient
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

    @Value(value = "${ss.client-header-name}")
    private String clientId;
    private static final String CLIENT_ID = "";

    @Value(value = "${ss.api-key-header-name}")
    private String apiKey;
    private static final String X_API_KEY = "";

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
        fileCreationRequest.setContentType("content-type");
        fileCreationRequest.setStatus("status");
        fileCreationRequest.setDocumentType("document-type");

        return fileCreationRequest;
    }

    @Test
    void convertPdf_Ok(){
        doReturn(Mono.just(FILE_CREATION_RESPONSE)).when(safeStorageCall).postFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));
        doReturn(Mono.just(SendMessageResponse.builder().build())).when(sqsService).send(anyString(),anyString());
//        when(safeStorageCall.postFile(anyString(),anyString(),anyString(),anyString(),eq(fileCreationRequest()))).thenReturn(Mono.just(FILE_CREATION_RESPONSE));
//        when(sqsService.send(any(),any())).thenReturn(SendMessageResponse.builder().build());

        EntityExchangeResult<PdfRasterResponse> responseEntity = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(CONVERT_PDF_PATH).build(FILE_KEY_MOCK))
                .header(clientId, CLIENT_ID)
                .header(apiKey, X_API_KEY)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PdfRasterResponse.class)
                .returnResult();

        Assertions.assertNotNull(responseEntity.getResponseBody());
        Assertions.assertNotNull(responseEntity.getResponseBody().getNewFileKey());

        verify(safeStorageCall, times(1)).postFile(any(),any(),any(),any(),any());
        verify(sqsService, times(1)).send(any(),any());
    }

    @ParameterizedTest
    @MethodSource("exceptionSafeStorage")
    void convertPdf_Ko_SS(Exception e){
        /*
            Gestisco i vari casi di errore provenienti da SS
         */
        doReturn(Mono.error(e)).when(safeStorageCall).postFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));

        EntityExchangeResult<PdfRasterResponse> responseEntity = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(CONVERT_PDF_PATH).build(FILE_KEY_MOCK))
                .header(clientId, CLIENT_ID)
                .header(apiKey, X_API_KEY)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PdfRasterResponse.class)
                .returnResult();

        /*
            verifico che comunque safeStorage sia chiamato almeno una volta
         */
        verify(safeStorageCall,times(1)).postFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));
        /*
            verifico che le code non siano mai chiamate dal momento che l'applicazione dovrebbe andare in errore prima
         */
        verify(sqsService, never()).send(any(),any());

        /*
            Verifico che la ResponseEntity non abbia la fileKey valorizzata
         */
        Assertions.assertNotNull(responseEntity.getResponseBody());
        Assertions.assertNull(responseEntity.getResponseBody().getNewFileKey());
    }

    @Test
    void convertPdf_Ko_Sqs(){
        doReturn(Mono.just(FILE_CREATION_RESPONSE)).when(safeStorageCall).postFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));
        /*
            Gestisco il caso in cui sqs vada in errore , qualunque esso sia
         */
        doReturn(Mono.just(SendMessageResponse.builder().build())).when(sqsService).send(anyString(),anyString());

        EntityExchangeResult<PdfRasterResponse> responseEntity = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(CONVERT_PDF_PATH).build(FILE_KEY_MOCK))
                .header(clientId, CLIENT_ID)
                .header(apiKey, X_API_KEY)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PdfRasterResponse.class)
                .returnResult();

        /*
            verifico che la chiamata a Safe Storage vada a buon fine almeno una volta
         */
        verify(safeStorageCall,times(1)).postFile(anyString(),anyString(),anyString(),anyString(),eq(FILE_CREATION_REQUEST));

        /*
            Verifico che la chiamata alle code si verifichi almeno una volta
         */
        verify(sqsService, times(1)).send(any(),any());

        /*
            Verifico che la ResponseEntity non abbia la fileKey valorizzata
         */
        Assertions.assertNotNull(responseEntity.getResponseBody());
        Assertions.assertNull(responseEntity.getResponseBody().getNewFileKey());
    }
    /**
     * KO:
     *  - 3 Exception per SS (404,403,400)
     *  - Gestire caso di errore SQS
     */

    private static Stream<Arguments> exceptionSafeStorage(){
        return Stream.of(
                Arguments.of(new AttachmentNotAvailableException("")),
                Arguments.of(new ClientNotAuthorizedException("")),
                Arguments.of(new Generic400ErrorException("","")),
                Arguments.of(new Generic500ErrorException("",""))
        );
    }
}
