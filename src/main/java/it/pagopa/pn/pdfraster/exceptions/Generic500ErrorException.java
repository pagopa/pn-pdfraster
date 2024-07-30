package it.pagopa.pn.pdfraster.exceptions;

public class Generic500ErrorException extends GenericHttpStatusException {

    public Generic500ErrorException(String title, String details) {
        super(title, details);
    }
}