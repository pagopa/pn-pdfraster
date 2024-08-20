package it.pagopa.pn.pdfraster.rest;

import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.api.PdfRasterApi;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import lombok.CustomLog;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CustomLog
public class PdfRasterApiController implements PdfRasterApi {

    private final PdfRasterService pdfRasterService;

    public PdfRasterApiController(PdfRasterService pdfRasterService){
        this.pdfRasterService = pdfRasterService;
    }

    @Override
    public ResponseEntity<Resource> convertPdf(MultipartFile file) {
        log.info("");
        byte[] fileContent = null;
        try {
            fileContent = file.getBytes();
        } catch (IOException e) {
            throw new Generic400ErrorException("",e.getMessage());
        }
        Resource resource = pdfRasterService.convertPdf(fileContent);
        return ResponseEntity.ok(resource);
    }
}
