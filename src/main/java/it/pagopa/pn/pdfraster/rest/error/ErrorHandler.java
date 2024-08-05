package it.pagopa.pn.pdfraster.rest.error;

import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.pdfraster.exceptions.*;
import lombok.CustomLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@CustomLog
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ClientNotAuthorizedException.class)
    public final ResponseEntity<Problem> handleUnauthorizedIdClient(ClientNotAuthorizedException exception) {
        var problem = new Problem();
        problem.setStatus(FORBIDDEN.value());
        problem.setTitle("Client id not authorized");
        problem.setDetail(exception.getMessage());
        problem.setTraceId(UUID.randomUUID().toString());
        log.warn("clientId not authorized in handleUnauthorizedIdClient: {}", exception.getIdClient());

        return new ResponseEntity<>(problem, FORBIDDEN);
    }

    @ExceptionHandler(Generic400ErrorException.class)
    public final ResponseEntity<Problem> handleBadRequest(Exception exception) {
        var problem = new Problem();
        problem.setStatus(BAD_REQUEST.value());
        problem.setTitle("Bad request");
        problem.setDetail("Malformed request");
        problem.setTraceId(UUID.randomUUID().toString());
        return new ResponseEntity<>(problem, BAD_REQUEST);
    }

    @ExceptionHandler({SqsClientException.class/*, SnsSendException.class */})
    public final ResponseEntity<Problem> handleAnotherServiceError(Exception exception) {
        var problem = new Problem();
        problem.setStatus(SERVICE_UNAVAILABLE.value());
        problem.setTitle("System outage");
        problem.setDetail(exception.getMessage());
        problem.setTraceId(UUID.randomUUID().toString());
        return new ResponseEntity<>(problem, SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(AttachmentNotAvailableException.class)
    public final ResponseEntity<Problem> handleAttachmentNotAvailable(AttachmentNotAvailableException exception) {
        var problem = new Problem();
        problem.setStatus(NOT_FOUND.value());
        problem.setTitle("Attachment not found");
        problem.setDetail(exception.getMessage());
        problem.setTraceId(UUID.randomUUID().toString());
        return new ResponseEntity<>(problem, NOT_FOUND);
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
