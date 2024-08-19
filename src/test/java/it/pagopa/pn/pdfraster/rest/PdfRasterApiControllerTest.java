package it.pagopa.pn.pdfraster.rest;

import it.pagopa.pn.pdfraster.utils.RestClientsDetails;
import lombok.CustomLog;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;


@RunWith(SpringRunner.class)
@RestClientTest(RestClientsDetails.class)
@CustomLog
class PdfRasterApiControllerTest {

    @Autowired
    private RestClientsDetails restClientsDetails;

    private final static MultipartFile FILE_TEST_OK = getMultipartFileTest();

    /**
     * Metodo per recuperare il file di test dalle risorse
     * @return
     */
    public static byte[] getFileTestFromResources(){
        try (var in = new FileInputStream("src/test/resources/TEST.pdf")){
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Metodo per creare un MultipartFile contente il file di test
     * @return
     */
    public static MultipartFile getMultipartFileTest(){
        byte[] content = getFileTestFromResources();
        try {
            return new MockMultipartFile("TEST","TEST","application/pdf",new ByteArrayInputStream(content));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void convertPdf_OK(){
        Resource response = restClientsDetails.convertPdf(FILE_TEST_OK);


    }



}
