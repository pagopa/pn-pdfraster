package it.pagopa.pn.pdfraster.rest.error;

import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.exceptions.Generic500ErrorException;
import lombok.CustomLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@CustomLog
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({MultipartException.class,
                       Generic400ErrorException.class})
    public final ResponseEntity<Void> handleBadRequest(Exception exception) {
        log.warn("Bad Request", exception);
        return ResponseEntity.status(BAD_REQUEST).build();
    }

    @ExceptionHandler({Generic500ErrorException.class,Exception.class})
    public final ResponseEntity<Void> handleGenericError(Exception exception) {
        log.warn("Internal Server Error", exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
    }

}
