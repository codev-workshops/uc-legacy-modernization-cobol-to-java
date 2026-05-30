package com.carddemo.transaction.exception;

import com.carddemo.common.exception.BusinessValidationException;
import com.carddemo.common.exception.DuplicateResourceException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void handleBusinessValidation() {
        BusinessValidationException ex = new BusinessValidationException("Validation failed");
        ResponseEntity<ErrorResponse> response = handler.handleBusinessValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    void handleDuplicateResource() {
        DuplicateResourceException ex = new DuplicateResourceException("Duplicate");
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResource(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Duplicate", response.getBody().getMessage());
    }

    @Test
    void handleGeneral() {
        Exception ex = new RuntimeException("Unexpected");
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
