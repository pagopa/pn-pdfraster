package it.pagopa.pn.pdfraster.exceptions;

import lombok.Getter;

@Getter
public class ClientNotAuthorizedException extends RuntimeException {

    private final String idClient;
    public ClientNotAuthorizedException(String idClient) {
        super(String.format("Client id '%s' is unauthorized", idClient));
        this.idClient = idClient;
    }
}