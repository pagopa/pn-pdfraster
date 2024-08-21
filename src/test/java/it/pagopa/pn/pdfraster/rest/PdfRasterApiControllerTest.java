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
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import static it.pagopa.pn.pdfraster.utils.TestUtils.getFileKoTestFromResources;
import static it.pagopa.pn.pdfraster.utils.TestUtils.getFileTestFromResources;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_PDF;

@SpringBootTestWebEnv
@AutoConfigureWebTestClient(timeout = "100000")
@CustomLog
class PdfRasterApiControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final String CONVERT_PDF_PATH = "/pdf-raster/convert";

    private static final byte[] FILE;
    private static final byte[] FILE_KO;
    private final static MultiValueMap<String, HttpEntity<?>> FILE_TEST_OK;
    private final static MultiValueMap<String, HttpEntity<?>> FILE_TEST_KO;
    private final static MultiValueMap<String, HttpEntity<?>> FILE_TEST_EMPTY;

    static{
        FILE = getFileTestFromResources();
        FILE_KO = getFileKoTestFromResources();
        FILE_TEST_OK = getMultipartFileTest(FILE);
        FILE_TEST_KO = getMultipartFileTest(FILE_KO);
        FILE_TEST_EMPTY = getMultipartFileTest(new byte[0]);

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
                }).header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=file; filename=TEST.pdf")
                .contentType(APPLICATION_PDF);

        return builder.build();
    }

    /**
     *
     * @return
     */
    private @NotNull WebTestClient.ResponseSpec getResponseEntityExchangeResult(MultiValueMap<String, HttpEntity<?>> body) {
        return webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(CONVERT_PDF_PATH).build())
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



}
