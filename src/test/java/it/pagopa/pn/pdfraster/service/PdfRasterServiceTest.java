package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.test.StepVerifier;

import java.io.IOException;

import static it.pagopa.pn.pdfraster.utils.TestUtils.getFileKoTestFromResources;
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
        FILE_KO = getFileKoTestFromResources();
    }

    @Test
    void conversionePdf() {
        StepVerifier.create(convertPdfService.convertPdfToImage(FILE)).expectNextMatches(byteArrayOutputStream -> {
            try (PDDocument documentConverted = Loader.loadPDF(byteArrayOutputStream.toByteArray()); PDDocument documentOriginal = Loader.loadPDF(FILE)) {
                return documentConverted.getNumberOfPages() == documentOriginal.getNumberOfPages();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).verifyComplete();
    }
    @Test
    void conversionePdf_KO_WrongFile() {
        StepVerifier.create(convertPdfService.convertPdfToImage(FILE_KO)).expectError().verify();
    }

    @Test
    void conversionePdf_KO_EmptyFile() {
        StepVerifier.create(convertPdfService.convertPdfToImage(new byte[0])).expectError().verify();
    }
}
