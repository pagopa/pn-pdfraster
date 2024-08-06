package it.pagopa.pn.pdfraster.service;

import io.awspring.cloud.messaging.listener.Acknowledgment;
import it.pagopa.pn.pdfraster.model.pojo.DocumentQueueDto;
import it.pagopa.pn.pdfraster.rest.call.S3Call;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.service.impl.PdfRasterMessageReceiver;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationRequest;
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

import static it.pagopa.pn.pdfraster.testutils.TestUtils.*;
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
    private S3Call downloadCall;

    private static final byte[] FILE;
    private static final DocumentQueueDto DOCUMENT_QUEUE_DTO;

    private static final FileCreationResponse FILE_CREATION_RESPONSE;
    private static final FileCreationRequest FILE_CREATION_REQUEST;

    static {
        FILE_CREATION_REQUEST = fileCreationRequestInit();
        FILE_CREATION_RESPONSE = fileCreationResponseInit();
        DOCUMENT_QUEUE_DTO = documentQueueMockBean();
        FILE = getFileTestFromResources();
    }

    @Test
    void lavorazionePdfRasterDocuments(){

        DocumentQueueDto documentQueueDto = DocumentQueueDto.builder().build();
        /*
            Chiamata a safe storage per recuperare la Uri di download
         */
        doReturn(any()).when(safeStorageCall).getFile(anyString(),anyString(),anyString(),anyString());

        /*
            Chiamata a S3 per recuperare il file
         */
        doReturn(FILE).when(downloadCall).downloadFile(anyString());

        /*
            Caricamento su S3 del nuovo file convertito
         */


        /*
            Start flusso
         */
        pdfRasterMessageReceiver.lavorazionePdfRasterDocuments(DOCUMENT_QUEUE_DTO,acknowledgment);

        verify(safeStorageCall,times(1)).getFile(anyString(),anyString(),anyString(),anyString());
        verify(downloadCall,times(1)).downloadFile(anyString());
        verify(convertPdfService, times(1)).convertPdfToImage(any());
        verify(safeStorageCall,times(1)).createFile(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void safeStorage_Ko_PresignedUrl(){

        doReturn(Mono.error(new Exception())).when(safeStorageCall).getFile(anyString(),anyString(),anyString(),anyString());

        pdfRasterMessageReceiver.lavorazionePdfRasterDocuments(DOCUMENT_QUEUE_DTO,acknowledgment);

        verify(safeStorageCall,times(1)).getFile(anyString(),anyString(),anyString(),anyString());
        verify(downloadCall,never()).downloadFile(anyString());
        verify(convertPdfService,never()).convertPdfToImage(any());
        verify(safeStorageCall,never()).createFile(anyString(), anyString(), anyString(), anyString(), any());


    }

    @Test
    void safeStorage_Ko_CreateFile(){
        doReturn(any()).when(safeStorageCall).getFile(anyString(),anyString(),anyString(),anyString());

        doReturn(FILE).when(downloadCall).downloadFile(anyString());

        doReturn(Mono.error(new Exception())).when(safeStorageCall).createFile(anyString(), anyString(), anyString(), anyString(), any());

        pdfRasterMessageReceiver.lavorazionePdfRasterDocuments(DOCUMENT_QUEUE_DTO,acknowledgment);

        verify(safeStorageCall,times(1)).getFile(anyString(),anyString(),anyString(),anyString());
        verify(downloadCall,times(1)).downloadFile(anyString());
        verify(convertPdfService, times(1)).convertPdfToImage(any());
        verify(safeStorageCall,times(1)).createFile(anyString(), anyString(), anyString(), anyString(), any());

    }

    @Test
    void s3_Ko_getFile(){
        doReturn(any()).when(safeStorageCall).getFile(anyString(),anyString(),anyString(),anyString());

        doReturn(Mono.error(new Exception())).when(downloadCall).downloadFile(anyString());

        pdfRasterMessageReceiver.lavorazionePdfRasterDocuments(DOCUMENT_QUEUE_DTO,acknowledgment);

        verify(safeStorageCall,times(1)).getFile(anyString(),anyString(),anyString(),anyString());
        verify(downloadCall,times(1)).downloadFile(anyString());
        verify(convertPdfService, never()).convertPdfToImage(any());
        verify(safeStorageCall,never()).createFile(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void conversionePdf_Ko(){

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
