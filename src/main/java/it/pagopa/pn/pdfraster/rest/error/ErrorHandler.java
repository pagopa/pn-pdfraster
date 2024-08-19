package it.pagopa.pn.pdfraster.rest.error;

import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.pdfraster.exceptions.Generic400ErrorException;
import it.pagopa.pn.pdfraster.exceptions.Generic500ErrorException;
import lombok.CustomLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@CustomLog
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(Generic400ErrorException.class)
    public final ResponseEntity<Problem> handleBadRequest(Exception exception) {
        var problem = new Problem();
        problem.setStatus(BAD_REQUEST.value());
        problem.setTitle("Bad request");
        problem.setDetail("Malformed request");
        problem.setTraceId(UUID.randomUUID().toString());
        return new ResponseEntity<>(problem, BAD_REQUEST);
    }

    @ExceptionHandler({Exception.class, Generic500ErrorException.class})
    public final ResponseEntity<Problem> handleGenericError(Exception exception) {
        var problem = new Problem();
        problem.setStatus(INTERNAL_SERVER_ERROR.value());
        problem.setTitle("Internal Server Error");
        problem.setDetail(exception.getMessage());
        problem.setTraceId(UUID.randomUUID().toString());
        log.warn("Internal Server Error", exception);
        return new ResponseEntity<>(problem, INTERNAL_SERVER_ERROR);
    }

}
