package it.pagopa.pn.pdfraster.service;

import io.awspring.cloud.messaging.listener.Acknowledgment;
import it.pagopa.pn.pdfraster.model.pojo.DocumentQueueDto;
import it.pagopa.pn.pdfraster.rest.call.S3Call;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.service.impl.PdfRasterMessageReceiver;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationResponse;
import it.pagopa.pn.pdfraster.testutils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static it.pagopa.pn.pdfraster.testutils.TestUtils.documentQueueMockBean;
import static it.pagopa.pn.pdfraster.testutils.TestUtils.getFileTestFromResources;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTestWebEnv
@CustomLog
class PdfRasterServiceTest {

    @SpyBean
    private PdfRasterService pdfRasterService;
    @MockBean
    private SqsService sqsService;
    @Autowired
    private ConvertPdfService convertPdfService;
    @MockBean
    private PdfRasterMessageReceiver pdfRasterMessageReceiver;
    @MockBean
    private Acknowledgment acknowledgment;
    @MockBean
    private SafeStorageCall safeStorageCall;
    @MockBean
    private S3Call s3Call;

    private static final byte[] FILE;
    private static final DocumentQueueDto DOCUMENT_QUEUE_DTO;

    static {
        DOCUMENT_QUEUE_DTO = documentQueueMockBean();
        FILE = getFileTestFromResources();
    }

    @Test
    void lavorazionePdfRasterDocuments(){

        /*
            Chiamata a safe storage per recuperare la Uri di download
         */
        doReturn(Mono.just(new FileCreationResponse())).when(safeStorageCall).getFile(anyString(),anyString(),anyString(),anyString());

        /*
            Chiamata a S3 per recuperare il file
         */
        doReturn(Mono.just(FILE)).when(s3Call).downloadFile(anyString());

        /*
            Caricamento su S3 del nuovo file convertito
         */
        doReturn(Mono.empty()).when(s3Call).uploadFile(DOCUMENT_QUEUE_DTO.getUploadUrl(),FILE);

        /*
            Start flusso
         */
        pdfRasterMessageReceiver.lavorazionePdfRasterDocuments(DOCUMENT_QUEUE_DTO,acknowledgment);

        verify(safeStorageCall,times(1)).getFile(anyString(),anyString(),anyString(),anyString());
        verify(s3Call,times(1)).downloadFile(anyString());
        verify(convertPdfService, times(1)).convertPdfToImage(any());
        verify(s3Call,times(1)).uploadFile(anyString(),any());
    }

    @Test
    void safeStorage_Ko_PresignedUrl(){

        doReturn(Mono.error(new Exception())).when(safeStorageCall).getFile(anyString(),anyString(),anyString(),anyString());

        pdfRasterMessageReceiver.lavorazionePdfRasterDocuments(DOCUMENT_QUEUE_DTO,acknowledgment);

        verify(safeStorageCall,times(1)).getFile(anyString(),anyString(),anyString(),anyString());
        verify(s3Call,never()).downloadFile(anyString());
        verify(convertPdfService,never()).convertPdfToImage(any());
        verify(s3Call,never()).uploadFile(anyString(),any());
    }

    @Test
    void safeStorage_Ko_CreateFile(){
        doReturn(Mono.just(new FileCreationResponse())).when(safeStorageCall).getFile(anyString(),anyString(),anyString(),anyString());

        doReturn(Mono.just(FILE)).when(s3Call).downloadFile(anyString());

        doReturn(Mono.error(new Exception())).when(s3Call).uploadFile(DOCUMENT_QUEUE_DTO.getUploadUrl(),FILE);

        pdfRasterMessageReceiver.lavorazionePdfRasterDocuments(DOCUMENT_QUEUE_DTO,acknowledgment);

        verify(safeStorageCall,times(1)).getFile(anyString(),anyString(),anyString(),anyString());
        verify(s3Call,times(1)).downloadFile(anyString());
        verify(convertPdfService, times(1)).convertPdfToImage(any());
        verify(s3Call,times(1)).uploadFile(anyString(),any());

    }

    @Test
    void s3_Ko_getFile(){
        doReturn(Mono.just(new FileCreationResponse())).when(safeStorageCall).getFile(anyString(),anyString(),anyString(),anyString());

        doReturn(Mono.error(new Exception())).when(s3Call).downloadFile(anyString());

        pdfRasterMessageReceiver.lavorazionePdfRasterDocuments(DOCUMENT_QUEUE_DTO,acknowledgment);

        verify(safeStorageCall,times(1)).getFile(anyString(),anyString(),anyString(),anyString());
        verify(s3Call,times(1)).downloadFile(anyString());
        verify(convertPdfService, never()).convertPdfToImage(any());
        verify(s3Call,never()).uploadFile(anyString(),any());
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
}
