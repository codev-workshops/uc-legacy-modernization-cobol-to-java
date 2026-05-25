package com.carddemo.harness.validation;

import java.math.BigDecimal;
import java.util.List;

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
}
