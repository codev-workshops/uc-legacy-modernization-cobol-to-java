package com.carddemo.batch.cbact01c;

/**
 * Custom exception mirroring the COBOL abend behavior on I/O errors.
 */
public class FileProcessingException extends RuntimeException {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
