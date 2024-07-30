package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.FileCreationResponse;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.FileDownloadResponse;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.testutils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import static org.mockito.ArgumentMatchers.any;
import static reactor.core.publisher.Mono.when;
import static org.mockito.Mockito.*;


@SpringBootTestWebEnv
@CustomLog
public class PdfRasterServiceTest {

    @SpyBean
    private PdfRasterService pdfRasterService;
    @MockBean
    private SqsService sqsService;
    @MockBean
    private SafeStorageCall safeStorageCall;



    private static final FileCreationResponse FILE_CREATION_RESPONSE = fileCreationResponseInit();

    /**
     * Metodo per la creazione di un file Mock di risposta da SS
     * @return
     */
    private static FileCreationResponse fileCreationResponseInit(){
        FileCreationResponse response = new FileCreationResponse();
        response.setKey("");
        response.setSecret("");
        response.setUploadUrl("");
        response.setUploadMethod(null);

        return response;
    }

    /**
     * Metodo principale chiamato per la conversione
     */
    @Test
    void convertPdf_Ok(){

        //1) Chiamata a safe storage
        //2) Chiamata a SQS {PUBLISH}
        //3) Chiamata a SQS {SUBSCRIBE}
        //4) Chiamata a S3 {PRESIGNED URL DOWNLOAD}
        //5) Chiamata a S3 {DOWNLOAD PDF}
        //6) conversione contenuto PDF
        //7) Chiamata a S3 {UPLOAD NEW PDF}

        // 1°
        when(safeStorageCall.postFile(any(),any(),any(),any(),any())).thenReturn(FILE_CREATION_RESPONSE);

        // 2°
        when(sqsService.send(any(),any())).thenReturn(SendMessageResponse.builder().build());

        //Chiamata al servizio
        Mono<String> response = pdfRasterService.convertPdf("");

        StepVerifier.create(response).expectNextCount(1).verifyComplete();

        verify(sqsService,times(1)).getMessages(any(),any());
    }

    /**
     * Metodo che utilizza il framework {NomeFramework} per convertire il contenuto del PDF in una image
     */
    @Test
    void convertPdfToImage(){

    }


}
