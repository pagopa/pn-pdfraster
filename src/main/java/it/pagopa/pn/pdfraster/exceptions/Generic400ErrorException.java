package it.pagopa.pn.pdfraster.exceptions;

public class Generic400ErrorException extends GenericHttpStatusException {

    public Generic400ErrorException(String title, String details) {
        super(title, details);
    }
}