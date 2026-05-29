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

    // Rule 8: Interest amount calculation

    @Test
    void interestAmountWithinTolerance() {
        // balance=1200, rate=18.0 → expected = 1200 * 18 / 1200 = 18.00
        ValidationResult result = validator.validateInterestAmount(
                new BigDecimal("1200.00"),
                new BigDecimal("18.0"),
                new BigDecimal("18.00"),
                new BigDecimal("0.01"));
        assertTrue(result.isPassed());
    }

    @Test
    void interestAmountExactMatch() {
        // balance=1000, rate=12.0 → expected = 1000 * 12 / 1200 = 10.00
        ValidationResult result = validator.validateInterestAmount(
                new BigDecimal("1000.00"),
                new BigDecimal("12.0"),
                new BigDecimal("10.00"),
                BigDecimal.ZERO);
        assertTrue(result.isPassed());
    }

    @Test
    void interestAmountRoundingFailsWithZeroTolerance() {
        // balance=1000, rate=7.0 → expected = 1000 * 7 / 1200 = 5.8333...
        // actual = 5.83 (COBOL truncation)
        ValidationResult result = validator.validateInterestAmount(
                new BigDecimal("1000.00"),
                new BigDecimal("7.0"),
                new BigDecimal("5.83"),
                BigDecimal.ZERO);
        assertFalse(result.isPassed());
    }

    @Test
    void interestAmountRoundingPassesWithTolerance() {
        // balance=1000, rate=7.0 → expected = 5.8333...
        // actual = 5.83, within 0.01 tolerance
        ValidationResult result = validator.validateInterestAmount(
                new BigDecimal("1000.00"),
                new BigDecimal("7.0"),
                new BigDecimal("5.83"),
                new BigDecimal("0.01"));
        assertTrue(result.isPassed());
    }

    @Test
    void interestAmountOutsideTolerance() {
        ValidationResult result = validator.validateInterestAmount(
                new BigDecimal("1000.00"),
                new BigDecimal("12.0"),
                new BigDecimal("15.00"),
                new BigDecimal("0.01"));
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Interest amount mismatch"));
    }

    // Rule 9: Interest transaction type

    @Test
    void interestTransactionTypeCorrect() {
        ValidationResult result = validator.validateInterestTransactionType("01", "0005");
        assertTrue(result.isPassed());
    }

    @Test
    void interestTransactionTypeWrongTypeCd() {
        ValidationResult result = validator.validateInterestTransactionType("02", "0005");
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("TRAN-TYPE-CD"));
    }

    @Test
    void interestTransactionTypeWrongCatCd() {
        ValidationResult result = validator.validateInterestTransactionType("01", "0001");
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("TRAN-CAT-CD"));
    }

    // Rule 10: Account active status

    @Test
    void accountActiveValid() {
        ValidationResult result = validator.validateAccountActive("Y");
        assertTrue(result.isPassed());
    }

    @Test
    void accountActiveInvalid() {
        ValidationResult result = validator.validateAccountActive("N");
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("not active"));
    }

    @Test
    void accountActiveNull() {
        ValidationResult result = validator.validateAccountActive(null);
        assertFalse(result.isPassed());
    }

    // Rule 11: Sort order verification

    @Test
    void sortOrderCorrect() {
        List<String> keys = Arrays.asList("001", "002", "003", "010");
        ValidationResult result = validator.validateSortOrder(keys);
        assertTrue(result.isPassed());
    }

    @Test
    void sortOrderViolation() {
        List<String> keys = Arrays.asList("001", "003", "002", "010");
        ValidationResult result = validator.validateSortOrder(keys);
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Sort order violation"));
    }

    @Test
    void sortOrderSingleElement() {
        ValidationResult result = validator.validateSortOrder(Collections.singletonList("001"));
        assertTrue(result.isPassed());
    }

    @Test
    void sortOrderEmpty() {
        ValidationResult result = validator.validateSortOrder(Collections.emptyList());
        assertTrue(result.isPassed());
    }

    // Rule 12: No duplicate keys

    @Test
    void noDuplicatesValid() {
        List<String> keys = Arrays.asList("001", "002", "003");
        ValidationResult result = validator.validateNoDuplicateKeys(keys);
        assertTrue(result.isPassed());
    }

    @Test
    void duplicatesFound() {
        List<String> keys = Arrays.asList("001", "002", "001");
        ValidationResult result = validator.validateNoDuplicateKeys(keys);
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Duplicate key"));
    }

    @Test
    void noDuplicatesEmpty() {
        ValidationResult result = validator.validateNoDuplicateKeys(Collections.emptyList());
        assertTrue(result.isPassed());
    }

    // Rule 13: Export sequence numbering

    @Test
    void sequenceNumberingCorrect() {
        List<Integer> seqNums = Arrays.asList(1, 2, 3, 4, 5);
        ValidationResult result = validator.validateSequenceNumbering(seqNums);
        assertTrue(result.isPassed());
    }

    @Test
    void sequenceNumberingGap() {
        List<Integer> seqNums = Arrays.asList(1, 2, 4, 5);
        ValidationResult result = validator.validateSequenceNumbering(seqNums);
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Sequence number gap"));
    }

    @Test
    void sequenceNumberingStartsWrong() {
        List<Integer> seqNums = Arrays.asList(0, 1, 2);
        ValidationResult result = validator.validateSequenceNumbering(seqNums);
        assertFalse(result.isPassed());
    }

    @Test
    void sequenceNumberingEmpty() {
        ValidationResult result = validator.validateSequenceNumbering(Collections.emptyList());
        assertTrue(result.isPassed());
    }

    // Rule 14: Report total consistency

    @Test
    void reportTotalsMatch() {
        List<BigDecimal> pageTotals = Arrays.asList(
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                new BigDecimal("150.00"));
        ValidationResult result = validator.validateReportTotals(pageTotals, new BigDecimal("450.00"));
        assertTrue(result.isPassed());
    }

    @Test
    void reportTotalsMismatch() {
        List<BigDecimal> pageTotals = Arrays.asList(
                new BigDecimal("100.00"),
                new BigDecimal("200.00"));
        ValidationResult result = validator.validateReportTotals(pageTotals, new BigDecimal("400.00"));
        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Report total mismatch"));
    }

    @Test
    void reportTotalsEmpty() {
        ValidationResult result = validator.validateReportTotals(
                Collections.emptyList(), BigDecimal.ZERO);
        assertTrue(result.isPassed());
    }
}
