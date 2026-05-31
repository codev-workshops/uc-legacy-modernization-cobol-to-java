package com.carddemo.common.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void resourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Account", "id", 123L);
        assertEquals("Account not found with id: '123'", ex.getMessage());
        assertEquals("Account", ex.getResourceName());
        assertEquals("id", ex.getFieldName());
        assertEquals(123L, ex.getFieldValue());
    }

    @Test
    void validationException_messageOnly() {
        ValidationException ex = new ValidationException("Invalid input");
        assertEquals("Invalid input", ex.getMessage());
        assertTrue(ex.getErrors().isEmpty());
    }

    @Test
    void validationException_withErrors() {
        Map<String, String> errors = Map.of("field1", "error1", "field2", "error2");
        ValidationException ex = new ValidationException("Validation failed", errors);
        assertEquals("Validation failed", ex.getMessage());
        assertEquals(2, ex.getErrors().size());
        assertEquals("error1", ex.getErrors().get("field1"));
    }

    @Test
    void authenticationException_messageOnly() {
        AuthenticationException ex = new AuthenticationException("Not authenticated");
        assertEquals("Not authenticated", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void authenticationException_withCause() {
        RuntimeException cause = new RuntimeException("root cause");
        AuthenticationException ex = new AuthenticationException("Auth failed", cause);
        assertEquals("Auth failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void businessRuleViolationException_messageOnly() {
        BusinessRuleViolationException ex = new BusinessRuleViolationException("Rule violated");
        assertEquals("Rule violated", ex.getMessage());
        assertNull(ex.getRuleCode());
    }

    @Test
    void businessRuleViolationException_withCode() {
        BusinessRuleViolationException ex = new BusinessRuleViolationException("BR001", "Credit limit exceeded");
        assertEquals("Credit limit exceeded", ex.getMessage());
        assertEquals("BR001", ex.getRuleCode());
    }
}
