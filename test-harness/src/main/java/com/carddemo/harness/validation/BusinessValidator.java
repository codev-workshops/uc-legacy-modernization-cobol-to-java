package com.carddemo.harness.validation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements reconciliation checks derived from CBTRN02C and CBACT04C.
 */
public class BusinessValidator {

    /**
     * Rule 1: Record count reconciliation.
     * WS-TRANSACTION-COUNT + WS-REJECT-COUNT = total input record count.
     * (app/cbl/CBTRN02C.cbl lines 184-186, 206-216)
     */
    public ValidationResult validateRecordCounts(long inputCount, long processedCount, long rejectCount) {
        if (inputCount != processedCount + rejectCount) {
            return ValidationResult.fail(
                    "Record count mismatch: input=%d, processed+rejected=%d+%d=%d",
                    inputCount, processedCount, rejectCount, processedCount + rejectCount);
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 2: Account balance integrity.
     * ACCT-CURR-BAL(new) = ACCT-CURR-BAL(old) + SUM(posted TRAN-AMT for that account).
     * (app/cbl/CBTRN02C.cbl lines 545-552)
     */
    public ValidationResult validateAccountBalance(BigDecimal priorBalance, BigDecimal newBalance,
                                                    BigDecimal sumPostedAmounts) {
        BigDecimal expected = priorBalance.add(sumPostedAmounts);
        if (expected.compareTo(newBalance) != 0) {
            return ValidationResult.fail(
                    "Account balance mismatch: prior=%s + posted=%s = %s, but actual=%s",
                    priorBalance, sumPostedAmounts, expected, newBalance);
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 3: Cycle credit/debit split integrity.
     * ACCT-CURR-CYC-CREDIT = SUM(positive TRAN-AMT posted this cycle)
     * ACCT-CURR-CYC-DEBIT = SUM(negative TRAN-AMT posted this cycle) [as negative number]
     */
    public ValidationResult validateCycleSplit(BigDecimal cycCredit, BigDecimal cycDebit,
                                                List<BigDecimal> postedAmounts) {
        BigDecimal expectedCredit = postedAmounts.stream()
                .filter(a -> a.compareTo(BigDecimal.ZERO) >= 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expectedDebit = postedAmounts.stream()
                .filter(a -> a.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (expectedCredit.compareTo(cycCredit) != 0) {
            return ValidationResult.fail(
                    "Cycle credit mismatch: expected=%s, actual=%s", expectedCredit, cycCredit);
        }
        if (expectedDebit.compareTo(cycDebit) != 0) {
            return ValidationResult.fail(
                    "Cycle debit mismatch: expected=%s, actual=%s", expectedDebit, cycDebit);
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 4: TCATBAL category balance = SUM(TRAN-AMT) for matching (acct, type, category).
     * (app/cbl/CBTRN02C.cbl lines 467-508)
     */
    public ValidationResult validateCategoryBalance(String acctId, String typeCode, String catCode,
                                                     BigDecimal tcatBalance,
                                                     List<BigDecimal> matchingTransAmounts) {
        BigDecimal expected = matchingTransAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (expected.compareTo(tcatBalance) != 0) {
            return ValidationResult.fail(
                    "TCATBAL mismatch for key %s/%s/%s: expected=%s, actual=%s",
                    acctId, typeCode, catCode, expected, tcatBalance);
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 5: Credit limit validation — reject if overlimit.
     * WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
     * Must be &lt;= ACCT-CREDIT-LIMIT.
     * (app/cbl/CBTRN02C.cbl lines 393-421)
     */
    public ValidationResult validateCreditLimit(BigDecimal cycCredit, BigDecimal cycDebit,
                                                 BigDecimal tranAmt, BigDecimal creditLimit) {
        BigDecimal tempBal = cycCredit.subtract(cycDebit).add(tranAmt);
        if (tempBal.compareTo(creditLimit) > 0) {
            return ValidationResult.fail(
                    "Overlimit: tempBal=%s exceeds creditLimit=%s (reason 102)", tempBal, creditLimit);
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 6: Expiration date validation — reject if transaction date &gt; expiration.
     * (app/cbl/CBTRN02C.cbl lines 414-420)
     */
    public ValidationResult validateExpiration(String acctExpirationDate, String tranOrigTs) {
        String tranDate = tranOrigTs.length() >= 10 ? tranOrigTs.substring(0, 10) : tranOrigTs;
        if (acctExpirationDate.compareTo(tranDate) < 0) {
            return ValidationResult.fail(
                    "Transaction after expiration (reason 103): acct expires %s, tran date %s",
                    acctExpirationDate, tranDate);
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 7: After CBACT04C interest posting, cycle fields reset to zero.
     * (app/cbl/CBACT04C.cbl lines 350-356)
     */
    public ValidationResult validatePostInterestReset(BigDecimal cycCredit, BigDecimal cycDebit) {
        if (cycCredit.compareTo(BigDecimal.ZERO) != 0 || cycDebit.compareTo(BigDecimal.ZERO) != 0) {
            return ValidationResult.fail(
                    "Post-interest cycle fields not reset: credit=%s, debit=%s", cycCredit, cycDebit);
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 8: Interest amount calculation.
     * expected = tcatBalance * intRate / 1200
     * Pass if abs(expected - actualInterest) &lt;= tolerance.
     * (RECONCILIATION_CHECKS.md line 94)
     */
    public ValidationResult validateInterestAmount(BigDecimal tcatBalance, BigDecimal intRate,
                                                    BigDecimal actualInterest, BigDecimal tolerance) {
        BigDecimal expected = tcatBalance.multiply(intRate)
                .divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        if (expected.subtract(actualInterest).abs().compareTo(tolerance) > 0) {
            return ValidationResult.fail(
                    "Interest amount mismatch: expected=%s (balance=%s × rate=%s / 1200), actual=%s, tolerance=%s",
                    expected.toPlainString(), tcatBalance.toPlainString(),
                    intRate.toPlainString(), actualInterest.toPlainString(), tolerance.toPlainString());
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 9: Interest transaction type.
     * Must be TRAN-TYPE-CD='01' and TRAN-CAT-CD='0005'.
     * (RECONCILIATION_CHECKS.md lines 100-102)
     */
    public ValidationResult validateInterestTransactionType(String typeCd, String catCd) {
        if (!"01".equals(typeCd)) {
            return ValidationResult.fail(
                    "Interest TRAN-TYPE-CD must be '01', got '%s'", typeCd);
        }
        if (!"0005".equals(catCd)) {
            return ValidationResult.fail(
                    "Interest TRAN-CAT-CD must be '0005', got '%s'", catCd);
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 10: Account active status validation.
     * Reject if ACCT-ACTIVE-STATUS is not 'Y'.
     * (RECONCILIATION_CHECKS.md line 61)
     */
    public ValidationResult validateAccountActive(String activeStatus) {
        if (!"Y".equals(activeStatus)) {
            return ValidationResult.fail(
                    "Account not active: ACCT-ACTIVE-STATUS='%s', expected 'Y'", activeStatus);
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 11: Sort order verification.
     * Verify keys are in ascending order.
     * (RECONCILIATION_CHECKS.md lines 186-187)
     */
    public ValidationResult validateSortOrder(List<String> keys) {
        for (int i = 1; i < keys.size(); i++) {
            if (keys.get(i).compareTo(keys.get(i - 1)) < 0) {
                return ValidationResult.fail(
                        "Sort order violation at index %d: '%s' < '%s'",
                        i, keys.get(i), keys.get(i - 1));
            }
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 12: No duplicate keys.
     * Verify all keys are unique.
     * (RECONCILIATION_CHECKS.md lines 189-190)
     */
    public ValidationResult validateNoDuplicateKeys(List<String> keys) {
        Set<String> seen = new HashSet<>();
        for (String key : keys) {
            if (!seen.add(key)) {
                return ValidationResult.fail("Duplicate key found: '%s'", key);
            }
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 13: Export sequence numbering.
     * Must be consecutive starting from 1.
     * (RECONCILIATION_CHECKS.md lines 232-233)
     */
    public ValidationResult validateSequenceNumbering(List<Integer> seqNums) {
        for (int i = 0; i < seqNums.size(); i++) {
            int expected = i + 1;
            if (seqNums.get(i) != expected) {
                return ValidationResult.fail(
                        "Sequence number gap at position %d: expected=%d, actual=%d",
                        i, expected, seqNums.get(i));
            }
        }
        return ValidationResult.pass();
    }

    /**
     * Rule 14: Report total consistency.
     * SUM(page totals) must equal grand total.
     * (RECONCILIATION_CHECKS.md lines 146-147)
     */
    public ValidationResult validateReportTotals(List<BigDecimal> pageTotals, BigDecimal grandTotal) {
        BigDecimal sum = pageTotals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(grandTotal) != 0) {
            return ValidationResult.fail(
                    "Report total mismatch: SUM(page totals)=%s, grand total=%s",
                    sum.toPlainString(), grandTotal.toPlainString());
        }
        return ValidationResult.pass();
    }
}
