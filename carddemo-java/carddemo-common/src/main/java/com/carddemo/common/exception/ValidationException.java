package com.carddemo.common.exception;

import java.util.Collections;
import java.util.Map;

public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = Collections.unmodifiableMap(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
