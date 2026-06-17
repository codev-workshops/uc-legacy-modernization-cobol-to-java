package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Main processing logic for CBACT01C — reads AccountRecords and produces
 * three output record lists: OutAccountRecord, ArrayRecord, and VBRC records.
 */
public class Cbact01cProcessor {

    private static final BigDecimal DEBIT_SUBSTITUTION = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_SLOT0 = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_SLOT1 = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_SLOT2 = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_SLOT2 = new BigDecimal("-2500.00");

    private final List<OutAccountRecord> outRecords = new ArrayList<>();
    private final List<ArrayRecord> arrayRecords = new ArrayList<>();
    private final List<Object> vbrcRecords = new ArrayList<>();

    /**
     * Process a list of account records and produce all output records.
     *
     * @param accounts input account records
     * @return this processor instance (for chaining)
     */
    public Cbact01cProcessor process(List<AccountRecord> accounts) {
        for (AccountRecord account : accounts) {
            processRecord(account);
        }
        return this;
    }

    private void processRecord(AccountRecord acct) {
        outRecords.add(buildOutRecord(acct));
        arrayRecords.add(buildArrayRecord(acct));
        vbrcRecords.add(buildVbrcRecord1(acct));
        vbrcRecords.add(buildVbrcRecord2(acct));
    }

    private OutAccountRecord buildOutRecord(AccountRecord acct) {
        String formattedReissueDate = DateFormatter.formatDate(acct.acctReissueDate());

        // Deviation from COBOL: always set the debit field.
        // When zero, use 2525.00; when nonzero, use the actual value.
        // In the original COBOL, when debit is nonzero the output field retains
        // whatever was there from the prior write (likely a bug).
        BigDecimal currCycDebit;
        if (acct.acctCurrCycDebit().compareTo(BigDecimal.ZERO) == 0) {
            currCycDebit = DEBIT_SUBSTITUTION;
        } else {
            currCycDebit = acct.acctCurrCycDebit();
        }

        return new OutAccountRecord(
                acct.acctId(),
                acct.acctActiveStatus(),
                acct.acctCurrBal(),
                acct.acctCreditLimit(),
                acct.acctCashCreditLimit(),
                acct.acctOpenDate(),
                acct.acctExpirationDate(),
                formattedReissueDate,
                acct.acctCurrCycCredit(),
                currCycDebit,
                acct.acctGroupId()
        );
    }

    private ArrayRecord buildArrayRecord(AccountRecord acct) {
        List<ArrayRecord.ArrayEntry> entries = new ArrayList<>(5);
        // Slot 0: bal = acctCurrBal, debit = 1005.00
        entries.add(new ArrayRecord.ArrayEntry(acct.acctCurrBal(), ARRAY_DEBIT_SLOT0));
        // Slot 1: bal = acctCurrBal, debit = 1525.00
        entries.add(new ArrayRecord.ArrayEntry(acct.acctCurrBal(), ARRAY_DEBIT_SLOT1));
        // Slot 2: bal = -1025.00, debit = -2500.00
        entries.add(new ArrayRecord.ArrayEntry(ARRAY_BAL_SLOT2, ARRAY_DEBIT_SLOT2));
        // Slots 3-4: bal = 0, debit = 0 (from INITIALIZE)
        entries.add(new ArrayRecord.ArrayEntry(BigDecimal.ZERO, BigDecimal.ZERO));
        entries.add(new ArrayRecord.ArrayEntry(BigDecimal.ZERO, BigDecimal.ZERO));

        return new ArrayRecord(acct.acctId(), entries);
    }

    private VbrcRecord1 buildVbrcRecord1(AccountRecord acct) {
        return new VbrcRecord1(acct.acctId(), acct.acctActiveStatus());
    }

    private VbrcRecord2 buildVbrcRecord2(AccountRecord acct) {
        // WS-ACCT-REISSUE-YYYY is the first 4 chars of the reissue date
        String reissueYear = "";
        if (acct.acctReissueDate() != null && acct.acctReissueDate().length() >= 4) {
            reissueYear = acct.acctReissueDate().substring(0, 4);
        }
        return new VbrcRecord2(acct.acctId(), acct.acctCurrBal(), acct.acctCreditLimit(), reissueYear);
    }

    public List<OutAccountRecord> getOutRecords() {
        return outRecords;
    }

    public List<ArrayRecord> getArrayRecords() {
        return arrayRecords;
    }

    public List<Object> getVbrcRecords() {
        return vbrcRecords;
    }
}
