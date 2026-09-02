package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.date.DateConversionRequest;
import com.carddemo.batch.cbact01c.date.DateFormatter;
import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrArrayRec;
import com.carddemo.batch.cbact01c.model.ArrArrayRec.ArrAcctBal;
import com.carddemo.batch.cbact01c.model.OutAcctRec;
import com.carddemo.batch.cbact01c.model.Vb1Rec;
import com.carddemo.batch.cbact01c.model.Vb2Rec;

import java.math.BigDecimal;

/** Per-record mapping paragraphs 1300 / 1400 / 1500 of CBACT01C as pure functions. */
public final class AccountProcessor {

    public static final BigDecimal INITIAL_CARRIED_DEBIT = new BigDecimal("0.00");
    public static final BigDecimal OUT_CYC_DEBIT = new BigDecimal("2525.00");
    public static final BigDecimal ARR_CYC_DEBIT_1 = new BigDecimal("1005.00");
    public static final BigDecimal ARR_CYC_DEBIT_2 = new BigDecimal("1525.00");
    public static final BigDecimal ARR_CURR_BAL_3 = new BigDecimal("-1025.00");
    public static final BigDecimal ARR_CYC_DEBIT_3 = new BigDecimal("-2500.00");

    private AccountProcessor() {
    }

    /**
     * 1300-POPUL-ACCT-RECORD. {@code carriedCurrCycDebit} is OUT-ACCT-CURR-CYC-DEBIT as left by the
     * previous record; it is only overwritten (with 2525.00) when the input debit is zero.
     */
    public static OutAcctRec populateOut(AccountRecord acct, BigDecimal carriedCurrCycDebit, DateFormatter df) {
        String reissue = df.convert(new DateConversionRequest('2', '2', acct.reissueDate())).outputDate10();
        BigDecimal debit = acct.currCycDebit().signum() == 0 ? OUT_CYC_DEBIT : carriedCurrCycDebit;
        return new OutAcctRec(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                reissue,
                acct.currCycCredit(),
                debit,
                acct.groupId());
    }

    /** INITIALIZE ARR-ARRAY-REC followed by 1400-POPUL-ARRAY-RECORD. */
    public static ArrArrayRec populateArr(AccountRecord acct) {
        return ArrArrayRec.initialized()
                .withAcctId(acct.acctId())
                .withBal(0, new ArrAcctBal(acct.currBal(), ARR_CYC_DEBIT_1))
                .withBal(1, new ArrAcctBal(acct.currBal(), ARR_CYC_DEBIT_2))
                .withBal(2, new ArrAcctBal(ARR_CURR_BAL_3, ARR_CYC_DEBIT_3));
    }

    /** 1500-POPUL-VBRC-RECORD, VBRC-REC1 part. */
    public static Vb1Rec vb1(AccountRecord acct) {
        return new Vb1Rec(acct.acctId(), acct.activeStatus());
    }

    /** 1500-POPUL-VBRC-RECORD, VBRC-REC2 part; YYYY comes from the original ACCT-REISSUE-DATE. */
    public static Vb2Rec vb2(AccountRecord acct) {
        return new Vb2Rec(acct.acctId(), acct.currBal(), acct.creditLimit(), acct.reissueDate().substring(0, 4));
    }
}
