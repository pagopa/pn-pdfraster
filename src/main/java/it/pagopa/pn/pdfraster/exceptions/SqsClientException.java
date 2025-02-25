package it.pagopa.pn.pdfraster.exceptions;

public class SqsClientException extends RuntimeException {

    public SqsClientException(String queueName) {
        super(String.format("An error occurred during client operation on '%s' queue", queueName));
    }
}
