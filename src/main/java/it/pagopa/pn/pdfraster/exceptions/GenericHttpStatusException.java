package it.pagopa.pn.pdfraster.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class GenericHttpStatusException extends RuntimeException{

    final String title;
    final String details;

    public GenericHttpStatusException(String title, String details) {
        super(String.format("%s: %s", title, details));
        this.title = title;
        this.details = details;
    }
}
