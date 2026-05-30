package com.carddemo.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void resourceNotFoundExceptionMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        assertThat(ex.getMessage()).isEqualTo("not found");
    }

    @Test
    void resourceNotFoundExceptionWithCause() {
        RuntimeException cause = new RuntimeException("root cause");
        ResourceNotFoundException ex = new ResourceNotFoundException("not found", cause);
        assertThat(ex.getMessage()).isEqualTo("not found");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void businessValidationExceptionMessage() {
        BusinessValidationException ex = new BusinessValidationException("validation failed");
        assertThat(ex.getMessage()).isEqualTo("validation failed");
    }

    @Test
    void businessValidationExceptionWithCause() {
        RuntimeException cause = new RuntimeException("root");
        BusinessValidationException ex = new BusinessValidationException("validation failed", cause);
        assertThat(ex.getMessage()).isEqualTo("validation failed");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void duplicateResourceExceptionMessage() {
        DuplicateResourceException ex = new DuplicateResourceException("duplicate");
        assertThat(ex.getMessage()).isEqualTo("duplicate");
    }

    @Test
    void duplicateResourceExceptionWithCause() {
        RuntimeException cause = new RuntimeException("root");
        DuplicateResourceException ex = new DuplicateResourceException("duplicate", cause);
        assertThat(ex.getMessage()).isEqualTo("duplicate");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void authenticationExceptionMessage() {
        AuthenticationException ex = new AuthenticationException("auth failed");
        assertThat(ex.getMessage()).isEqualTo("auth failed");
    }

    @Test
    void authenticationExceptionWithCause() {
        RuntimeException cause = new RuntimeException("root");
        AuthenticationException ex = new AuthenticationException("auth failed", cause);
        assertThat(ex.getMessage()).isEqualTo("auth failed");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void exceptionsAreRuntimeExceptions() {
        assertThat(new ResourceNotFoundException("x")).isInstanceOf(RuntimeException.class);
        assertThat(new BusinessValidationException("x")).isInstanceOf(RuntimeException.class);
        assertThat(new DuplicateResourceException("x")).isInstanceOf(RuntimeException.class);
        assertThat(new AuthenticationException("x")).isInstanceOf(RuntimeException.class);
    }
}
