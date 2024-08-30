package it.pagopa.pn.pdfraster.rest;

import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import static it.pagopa.pn.pdfraster.utils.TestUtils.getFileKoTestFromResources;
import static it.pagopa.pn.pdfraster.utils.TestUtils.getFileTestFromResources;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.*;

@SpringBootTestWebEnv
@AutoConfigureWebTestClient(timeout = "100000")
@CustomLog
class PdfRasterApiControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final String CONVERT_PDF_PATH = "/PDFRaster/convert";

    private static final byte[] FILE;
    private static final byte[] FILE_KO;
    private final static MultiValueMap<String, HttpEntity<?>> FILE_TEST_OK;
    private final static MultiValueMap<String, HttpEntity<?>> FILE_TEST_KO;
    private final static MultiValueMap<String, HttpEntity<?>> FILE_TEST_EMPTY;
    private final static MultiValueMap<String, HttpEntity<?>> FILE_TEST_WRONG_CONTENT_TYPE;

    static{
        FILE = getFileTestFromResources();
        FILE_KO = getFileKoTestFromResources();
        FILE_TEST_OK = getMultipartFileTest(FILE);
        FILE_TEST_KO = getMultipartFileTest(FILE_KO);
        FILE_TEST_EMPTY = getMultipartFileTest(new byte[0]);
        FILE_TEST_WRONG_CONTENT_TYPE = getMultipartFileTestWrongContentType(FILE);
    }

    /**
     * Metodo per creare un MultipartFile contente il file di test
     *
     * @return
     */
    public static MultiValueMap<String, HttpEntity<?>> getMultipartFileTest(byte[] file){
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("file", new ByteArrayResource(file) {
                    @Override
                    public String getFilename() {
                        return "TEST.pdf";
                    }
                });

        return builder.build();
    }

    /**
     * Metodo per creare un MultipartFile contente il file di test
     *
     * @return
     */
    public static MultiValueMap<String, HttpEntity<?>> getMultipartFileTestWrongContentType(byte[] file){
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("file", new ByteArrayResource(file) {
                    @Override
                    public String getFilename() {
                        return "TEST.pdf";
                    }
                }).header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=file; filename=TEST.pdf;content-type=application/json")
                .contentType(APPLICATION_JSON);

        return builder.build();
    }
    /**
     *
     * @return
     */
    private @NotNull WebTestClient.ResponseSpec getResponseEntityExchangeResult(MultiValueMap<String, HttpEntity<?>> body) {
        return webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(CONVERT_PDF_PATH).build())
                .contentType(MULTIPART_FORM_DATA)
                .accept(APPLICATION_PDF)
                .body(BodyInserters.fromMultipartData(body))
                .exchange();

    }

    /**
     *
     * @return
     */
    private @NotNull WebTestClient.ResponseSpec getResponseEntityExchangeResultMissingBody() {
        return webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(CONVERT_PDF_PATH).build())
                .contentType(MULTIPART_FORM_DATA)
                .accept(APPLICATION_PDF)
                .exchange();
    }

    /**
     *
     * @param body
     * @return
     */
    private @NotNull WebTestClient.ResponseSpec getResponseEntityExchangeResultWrongContentType(MultiValueMap<String, HttpEntity<?>> body) {
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "application/json; name=\"file\"; filename=\"TEST.pdf\"");
        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
        return webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(CONVERT_PDF_PATH).build())
                .headers(httpHeaders ->  httpHeaders.addAll(headers))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_PDF)
                .body(BodyInserters.fromMultipartData(body))
                .exchange();

    }

    @Test
    void convertPdf_OK(){
        getResponseEntityExchangeResult(FILE_TEST_OK)
                .expectStatus().isOk()
                .expectBody(Resource.class)
                .consumeWith(response -> {
                    assertNotNull(response);
                    Resource resource = response.getResponseBody();
                    assertNotNull(resource);
                });
    }

    @Test
    void convertPdf_KO(){
        getResponseEntityExchangeResult(FILE_TEST_KO)
                .expectStatus().is5xxServerError()
                .expectBody(Resource.class)
                .consumeWith(ex ->{
                    Assertions.assertNull(ex.getResponseBody());
                });
    }

    @Test
    void convertPdf_EmptyFile(){
        getResponseEntityExchangeResult(FILE_TEST_EMPTY)
                .expectStatus().isBadRequest()
                .expectBody(Resource.class)
                .consumeWith(ex ->{
                    Assertions.assertNull(ex.getResponseBody());
                });
    }

    @Test
    void convertPdf_ContentTypeMultipart_KO(){
        getResponseEntityExchangeResultWrongContentType(FILE_TEST_WRONG_CONTENT_TYPE)
                .expectStatus().isBadRequest()
                .expectBody(Resource.class)
                .consumeWith(ex ->{
                    Assertions.assertNull(ex.getResponseBody());
                });
    }

    @Test
    void convertPdf_MissingRequest_KO(){
        getResponseEntityExchangeResultMissingBody()
                .expectStatus().isBadRequest()
                .expectBody(Resource.class)
                .consumeWith(ex ->{
                    Assertions.assertNull(ex.getResponseBody());
                });
    }
}
