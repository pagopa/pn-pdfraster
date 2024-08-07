package it.pagopa.pn.pdfraster.exceptions;

public class JsonStringToObjectException extends RuntimeException {

    public <T> JsonStringToObjectException(String jsonString, Class<T> classToMap) {
        super(String.format("There was an error converting this JSON string into class %s ↓%n%s", classToMap.getSimpleName(), jsonString));
    }
}
