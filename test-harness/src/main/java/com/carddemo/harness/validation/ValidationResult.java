package com.carddemo.harness.validation;

/**
 * Result of a business validation rule check.
 */
public class ValidationResult {

    private final boolean passed;
    private final String message;

    private ValidationResult(boolean passed, String message) {
        this.passed = passed;
        this.message = message;
    }

    public static ValidationResult pass() {
        return new ValidationResult(true, "PASS");
    }

    public static ValidationResult fail(String format, Object... args) {
        return new ValidationResult(false, String.format(format, args));
    }

    public boolean isPassed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return passed ? "PASS" : "FAIL: " + message;
    }
}
