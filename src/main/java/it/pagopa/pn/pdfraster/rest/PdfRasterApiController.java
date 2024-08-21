package it.pagopa.pn.pdfraster.rest;

import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.api.PdfRasterApi;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static it.pagopa.pn.pdfraster.utils.LogUtils.CONVERT_PDF;
import static it.pagopa.pn.pdfraster.utils.LogUtils.INVALID_REQUEST;

@RestController
@CustomLog
public class PdfRasterApiController implements PdfRasterApi {

    private final PdfRasterService pdfRasterService;

    public PdfRasterApiController(PdfRasterService pdfRasterService){
        this.pdfRasterService = pdfRasterService;
    }

    @Override
    public ResponseEntity<Resource> convertPdf(MultipartFile file) {
        log.logStartingProcess(CONVERT_PDF);
        byte[] fileContent = getBytes(file);
        Resource resource = pdfRasterService.convertPdf(fileContent);
        log.logEndingProcess(CONVERT_PDF);
        return ResponseEntity.ok(resource);
    }

    /**
     *
     * @param file
     * @return
     */
    private static byte @NotNull [] getBytes(MultipartFile file) {
        byte[] fileContent = null;
        try {
            if(!"application/pdf".equalsIgnoreCase(file.getContentType())){
                throw new Generic400ErrorException(INVALID_REQUEST,"Wrong Content Type");
            }

            fileContent = file.getBytes();
            if(fileContent.length == 0){
                throw new Generic400ErrorException(INVALID_REQUEST,"File null or empty");
            }
        } catch (IOException e) {
            log.logEndingProcess(CONVERT_PDF,false,e.getMessage());
            throw new Generic400ErrorException(INVALID_REQUEST,e.getMessage());
        }
        return fileContent;
    }
}
