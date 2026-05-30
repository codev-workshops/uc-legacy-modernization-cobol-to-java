package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountOutputBundle;
import com.carddemo.batch.model.ArryfileRecord;
import com.carddemo.batch.model.OutfileRecord;
import com.carddemo.batch.model.VbrcRec1;
import com.carddemo.batch.model.VbrcRec2;
import com.carddemo.common.entity.Account;
import com.carddemo.common.util.DateFormatUtil;
import java.math.BigDecimal;
import org.springframework.batch.item.ItemProcessor;

/**
 * Mirrors CBACT01C paragraphs 1300, 1400, 1500.
 * Transforms an Account entity into the three output record types.
 */
public class AccountItemProcessor implements ItemProcessor<Account, AccountOutputBundle> {

    static final BigDecimal ZERO_DEBIT_SUBSTITUTE = new BigDecimal("2525.00");

    static final BigDecimal ARRAY_CYC_DEBIT_1 = new BigDecimal("1005.00");
    static final BigDecimal ARRAY_CYC_DEBIT_2 = new BigDecimal("1525.00");
    static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    static final BigDecimal ARRAY_CYC_DEBIT_3 = new BigDecimal("-2500.00");

    @Override
    public AccountOutputBundle process(Account account) {
        OutfileRecord outRec = buildOutfileRecord(account);
        ArryfileRecord arrRec = buildArryfileRecord(account);
        VbrcRec1 vb1 = buildVbrcRec1(account);
        VbrcRec2 vb2 = buildVbrcRec2(account);
        return new AccountOutputBundle(outRec, arrRec, vb1, vb2);
    }

    /**
     * 1300-POPUL-ACCT-RECORD: populates OUTFILE record.
     * Edge case 1 (CBACT01C.cbl:236-238): zero debit → 2525.00
     * Edge case 2 (CBACT01C.cbl:223-233): date truncation to 8 chars
     */
    OutfileRecord buildOutfileRecord(Account account) {
        OutfileRecord rec = new OutfileRecord();
        rec.setAcctId(account.getAcctId());
        rec.setActiveStatus(account.getActiveStatus());
        rec.setCurrBal(account.getCurrBal());
        rec.setCreditLimit(account.getCreditLimit());
        rec.setCashCreditLimit(account.getCashCreditLimit());
        rec.setOpenDate(account.getOpenDate());
        rec.setExpirationDate(account.getExpirationDate());

        // Date formatting: COBDATFT call at line 231
        // Input type "2" (YYYY-MM-DD), output type "2" (YYYY-MM-DD)
        // Only first 8 chars of 20-byte output are meaningful (edge case 2)
        String formatted = DateFormatUtil.formatDate(account.getReissueDate(), "2", "2");
        rec.setReissueDate(formatted.substring(0, 8));

        rec.setCurrCycCredit(account.getCurrCycCredit());

        // Edge case 1: zero debit substitution
        BigDecimal debit = account.getCurrCycDebit();
        if (debit == null || debit.compareTo(BigDecimal.ZERO) == 0) {
            rec.setCurrCycDebit(ZERO_DEBIT_SUBSTITUTE);
        } else {
            rec.setCurrCycDebit(debit);
        }

        rec.setGroupId(account.getGroupId());
        return rec;
    }

    /**
     * 1400-POPUL-ARRAY-RECORD: populates ARRY-FILE record.
     * Edge cases 3-4: Only indices 0-2 (COBOL 1-3) populated; hardcoded values.
     * Indices 3-4 (COBOL 4-5) remain at INITIALIZE values (zeros).
     */
    ArryfileRecord buildArryfileRecord(Account account) {
        ArryfileRecord rec = new ArryfileRecord();
        rec.setAcctId(account.getAcctId());

        // Slot 1: balance = ACCT-CURR-BAL, cycDebit = 1005.00
        rec.setBalance(0, account.getCurrBal());
        rec.setCycDebit(0, ARRAY_CYC_DEBIT_1);

        // Slot 2: balance = ACCT-CURR-BAL, cycDebit = 1525.00
        rec.setBalance(1, account.getCurrBal());
        rec.setCycDebit(1, ARRAY_CYC_DEBIT_2);

        // Slot 3: balance = -1025.00, cycDebit = -2500.00
        rec.setBalance(2, ARRAY_BAL_3);
        rec.setCycDebit(2, ARRAY_CYC_DEBIT_3);

        // Slots 4-5: remain at zero (INITIALIZE values)
        return rec;
    }

    /**
     * 1500-POPUL-VBRC-RECORD: populates both VB record types.
     */
    VbrcRec1 buildVbrcRec1(Account account) {
        VbrcRec1 rec = new VbrcRec1();
        rec.setAcctId(account.getAcctId());
        rec.setActiveStatus(account.getActiveStatus());
        return rec;
    }

    VbrcRec2 buildVbrcRec2(Account account) {
        VbrcRec2 rec = new VbrcRec2();
        rec.setAcctId(account.getAcctId());
        rec.setCurrBal(account.getCurrBal());
        rec.setCreditLimit(account.getCreditLimit());
        // WS-ACCT-REISSUE-YYYY: first 4 chars of reissue date
        String reissueDate = account.getReissueDate();
        if (reissueDate != null && reissueDate.length() >= 4) {
            rec.setReissueYyyy(reissueDate.substring(0, 4));
        } else {
            rec.setReissueYyyy("    ");
        }
        return rec;
    }
}
