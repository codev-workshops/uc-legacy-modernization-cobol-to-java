package com.carddemo.harness;

import com.carddemo.harness.validation.BusinessValidator;
import com.carddemo.harness.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BusinessValidatorTest {

    private BusinessValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BusinessValidator();
    }

    // Rule 1: Record count reconciliation

    @Test
    void recordCountsMatch() {
        ValidationResult result = validator.validateRecordCounts(100, 95, 5);
        assertTrue(result.isPassed());
    }

    @Test
    void recordCountsMismatch() {
        ValidationResult result = validator.validateRecordCounts(100, 90, 5);
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Record count mismatch"));
    }

    @Test
    void recordCountsAllProcessed() {
        ValidationResult result = validator.validateRecordCounts(50, 50, 0);
        assertTrue(result.isPassed());
    }

    // Rule 2: Account balance integrity

    @Test
    void accountBalanceCorrect() {
        ValidationResult result = validator.validateAccountBalance(
                new BigDecimal("1000.00"),
                new BigDecimal("1150.50"),
                new BigDecimal("150.50"));
        assertTrue(result.isPassed());
    }

    @Test
    void accountBalanceMismatch() {
        ValidationResult result = validator.validateAccountBalance(
                new BigDecimal("1000.00"),
                new BigDecimal("1200.00"),
                new BigDecimal("150.50"));
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Account balance mismatch"));
    }

    // Rule 3: Cycle credit/debit split

    @Test
    void cycleSplitCorrect() {
        List<BigDecimal> posted = Arrays.asList(
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("-30.00"));
        ValidationResult result = validator.validateCycleSplit(
                new BigDecimal("150.00"),
                new BigDecimal("-30.00"),
                posted);
        assertTrue(result.isPassed());
    }

    @Test
    void cycleSplitCreditMismatch() {
        List<BigDecimal> posted = Arrays.asList(
                new BigDecimal("100.00"),
                new BigDecimal("-30.00"));
        ValidationResult result = validator.validateCycleSplit(
                new BigDecimal("200.00"), // wrong
                new BigDecimal("-30.00"),
                posted);
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Cycle credit mismatch"));
    }

    // Rule 4: Category balance

    @Test
    void categoryBalanceCorrect() {
        List<BigDecimal> amounts = Arrays.asList(
                new BigDecimal("100.00"),
                new BigDecimal("200.00"));
        ValidationResult result = validator.validateCategoryBalance(
                "00000000001", "SA", "5001",
                new BigDecimal("300.00"), amounts);
        assertTrue(result.isPassed());
    }

    @Test
    void categoryBalanceMismatch() {
        ValidationResult result = validator.validateCategoryBalance(
                "00000000001", "SA", "5001",
                new BigDecimal("999.00"),
                Collections.singletonList(new BigDecimal("100.00")));
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("TCATBAL mismatch"));
    }

    // Rule 5: Credit limit validation

    @Test
    void creditLimitNotExceeded() {
        ValidationResult result = validator.validateCreditLimit(
                new BigDecimal("500.00"),
                new BigDecimal("-100.00"),
                new BigDecimal("200.00"),
                new BigDecimal("1000.00"));
        assertTrue(result.isPassed());
    }

    @Test
    void creditLimitExceeded() {
        ValidationResult result = validator.validateCreditLimit(
                new BigDecimal("900.00"),
                new BigDecimal("-50.00"),
                new BigDecimal("200.00"),
                new BigDecimal("1000.00"));
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Overlimit"));
    }

    // Rule 6: Expiration date validation

    @Test
    void transactionBeforeExpiration() {
        ValidationResult result = validator.validateExpiration(
                "2026-12-31", "2025-06-15-12.30.00.000000");
        assertTrue(result.isPassed());
    }

    @Test
    void transactionAfterExpiration() {
        ValidationResult result = validator.validateExpiration(
                "2024-12-31", "2025-06-15-12.30.00.000000");
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("reason 103"));
    }

    @Test
    void transactionOnExpirationDate() {
        ValidationResult result = validator.validateExpiration(
                "2025-06-15", "2025-06-15-12.30.00.000000");
        assertTrue(result.isPassed());
    }

    // Rule 7: Post-interest cycle reset

    @Test
    void cycleFieldsReset() {
        ValidationResult result = validator.validatePostInterestReset(
                BigDecimal.ZERO, BigDecimal.ZERO);
        assertTrue(result.isPassed());
    }

    @Test
    void cycleFieldsNotReset() {
        ValidationResult result = validator.validatePostInterestReset(
                new BigDecimal("100.00"), BigDecimal.ZERO);
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("not reset"));
    }
}
