package it.pagopa.pn.pdfraster.rest;

import it.pagopa.pn.pdfraster.pdfraster.rest.v1.api.PdfRasterApi;
import lombok.CustomLog;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CustomLog
public class PdfRasterApiController implements PdfRasterApi {


    @Override
    public ResponseEntity<Resource> convertPdf(MultipartFile file) {
        return PdfRasterApi.super.convertPdf(file);
    }
}
