package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static it.pagopa.pn.pdfraster.utils.TestUtils.getFileKotestFromResources;
import static it.pagopa.pn.pdfraster.utils.TestUtils.getFileTestFromResources;

@SpringBootTestWebEnv
@CustomLog
class PdfRasterServiceTest {

    @SpyBean
    private PdfRasterService pdfRasterService;
    @Autowired
    private ConvertPdfService convertPdfService;

    private static final byte[] FILE;
    private static final byte[] FILE_KO;

    static {
        FILE = getFileTestFromResources();
        FILE_KO = getFileKotestFromResources();
    }

    @Test
    void conversionePdf(){
        ByteArrayOutputStream outputStream = convertPdfService.convertPdfToImage(FILE);
        try (PDDocument documentConverted = Loader.loadPDF(outputStream.toByteArray()); PDDocument documentOriginal = Loader.loadPDF(FILE)) {
            // Quali altri test sulla conversione??
            Assertions.assertEquals(documentConverted.getNumberOfPages(), documentOriginal.getNumberOfPages());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void conversionePdf_KO_WrongFile(){
        ByteArrayOutputStream response = null;
        Exception ex = null;
        try {
            response = convertPdfService.convertPdfToImage(FILE_KO);
        } catch (Exception e) {
            ex = e;
        }

        Assertions.assertNotNull(ex);
        Assertions.assertNull(response);
    }

    @Test
    void conversionePdf_KO_EmptyFile(){
        ByteArrayOutputStream response = null;
        Exception ex = null;
        try {
            response = convertPdfService.convertPdfToImage(new byte[0]);
        } catch (Exception e) {
            ex = e;
        }

        Assertions.assertNotNull(ex);
        Assertions.assertNull(response);
    }
}
