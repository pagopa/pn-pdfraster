package it.pagopa.pn.pdfraster.rest;

import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.api.PdfRasterApi;
import it.pagopa.pn.pdfraster.service.PdfRasterService;
import lombok.CustomLog;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

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
    public Mono<ResponseEntity<Resource>> convertPdf(Flux<Part> file,final ServerWebExchange exchange) {
        log.logStartingProcess(CONVERT_PDF);
        return file.flatMap(this::getDataBuffer)
                .map(DataBuffer::asByteBuffer)
                .map(ByteBuffer::array)
                .flatMap(pdfRasterService::convertPdf)
                .reduce((b, b2) -> new ByteArrayResource(ArrayUtils.addAll(b.getByteArray(),b2.getByteArray())))
                .doOnSuccess(byteArrayResource -> log.logEndingProcess(CONVERT_PDF))
                .doOnError(throwable -> log.logEndingProcess(CONVERT_PDF,false,throwable.getMessage()))
                .map(ResponseEntity::ok);
    }

    /**
     * @param file
     * @return
     */
    private Flux<DataBuffer> getDataBuffer(Part file) {
        if(!MediaType.APPLICATION_PDF.equals(file.headers().getContentType())){
            throw new Generic400ErrorException(INVALID_REQUEST,"Wrong Content Type");
        }
        return file.content();
    }
}
