package it.pagopa.pn.pdfraster.exceptions;

public class AttachmentNotAvailableException extends Exception {

    public AttachmentNotAvailableException(String fileKey) {
        super(String.format("The file key '%s' is not available", fileKey));
    }
}