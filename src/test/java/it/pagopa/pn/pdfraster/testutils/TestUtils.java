package it.pagopa.pn.pdfraster.testutils;

import it.pagopa.pn.pdfraster.model.pojo.DocumentQueueDto;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationRequest;
import it.pagopa.pn.pdfraster.ss.rest.v1.dto.FileCreationResponse;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;

public abstract class TestUtils {

    private TestUtils(){
    }

    /**
     * Metodo per la creazione di un file Mock di risposta da SS
     * @return
     */
    public static FileCreationResponse fileCreationResponseInit(){
        FileCreationResponse response = new FileCreationResponse();
        response.setKey("key");
        response.setSecret("secret");
        response.setUploadUrl("url");
        response.setUploadMethod(FileCreationResponse.UploadMethodEnum.PUT);
        return response;
    }

    /**
     * Metodo per la creazione di un file di Mock per la request di creazione file ad SS
     * @return
     */
    public static FileCreationRequest fileCreationRequestInit(){
        FileCreationRequest fileCreationRequest = new FileCreationRequest();
        fileCreationRequest.setStatus("PRELOADED");
        fileCreationRequest.setContentType("application/pdf");
        fileCreationRequest.setDocumentType("PN_NOTIFICATION_ATTACHMENTS");
        return fileCreationRequest;
    }

    /**
     * Metodo per la creazione di un Dto di Mock per la lavorazione della coda
     * @return
     */
    public static DocumentQueueDto documentQueueMockBean(){
        return DocumentQueueDto.builder()
                .fileKey("FILEKEY")
                .secret("")
                .uploadUrl("UPLOAD-URL")
                .build();
    }

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
}
