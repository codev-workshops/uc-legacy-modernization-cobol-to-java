package com.carddemo.common.exception;

public class BusinessRuleViolationException extends RuntimeException {

    private final String ruleCode;

    public BusinessRuleViolationException(String message) {
        super(message);
        this.ruleCode = null;
    }

    public BusinessRuleViolationException(String ruleCode, String message) {
        super(message);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }
}
