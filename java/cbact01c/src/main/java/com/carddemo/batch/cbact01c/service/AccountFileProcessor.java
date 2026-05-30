package com.carddemo.batch.cbact01c.service;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayRecord;
import com.carddemo.batch.cbact01c.model.ArraySlot;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.model.VbRecord1;
import com.carddemo.batch.cbact01c.model.VbRecord2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Java equivalent of COBOL batch program CBACT01C.
 * Reads account records and produces three output streams:
 * <ul>
 *   <li>OutAccountRecord — main output with date conversion and debit substitution</li>
 *   <li>ArrayRecord — account ID + 5-slot balance/debit array</li>
 *   <li>VbRecord1/VbRecord2 — variable-length record pairs</li>
 * </ul>
 *
 * All financial amounts use {@link BigDecimal} with scale 2 and
 * {@link RoundingMode#DOWN} per RISK-01 in the project risk register.
 */
@Service
public class AccountFileProcessor {

    private static final BigDecimal DEBIT_SUBSTITUTION = new BigDecimal("2525.00");
    private static final BigDecimal SLOT1_DEBIT = new BigDecimal("1005.00");
    private static final BigDecimal SLOT2_DEBIT = new BigDecimal("1525.00");
    private static final BigDecimal SLOT3_BALANCE = new BigDecimal("-1025.00");
    private static final BigDecimal SLOT3_DEBIT = new BigDecimal("-2500.00");
    private static final int SCALE = 2;

    /**
     * Processes a list of input account records and produces the three output record sets.
     */
    public ProcessingResult process(List<AccountRecord> inputRecords) {
        List<OutAccountRecord> outRecords = new ArrayList<>();
        List<ArrayRecord> arrayRecords = new ArrayList<>();
        List<VbRecord1> vbRecords1 = new ArrayList<>();
        List<VbRecord2> vbRecords2 = new ArrayList<>();

        for (AccountRecord input : inputRecords) {
            outRecords.add(buildOutRecord(input));
            arrayRecords.add(buildArrayRecord(input));
            vbRecords1.add(buildVbRecord1(input));
            vbRecords2.add(buildVbRecord2(input));
        }

        return new ProcessingResult(outRecords, arrayRecords, vbRecords1, vbRecords2);
    }

    /**
     * Populates the output account record (OUTFILE).
     * Mirrors COBOL paragraph 1300-POPUL-ACCT-RECORD.
     */
    public OutAccountRecord buildOutRecord(AccountRecord input) {
        OutAccountRecord out = new OutAccountRecord();
        out.setAcctId(input.getAcctId());
        out.setActiveStatus(input.getActiveStatus());
        out.setCurrBal(scaled(input.getCurrBal()));
        out.setCreditLimit(scaled(input.getCreditLimit()));
        out.setCashCreditLimit(scaled(input.getCashCreditLimit()));
        out.setOpenDate(input.getOpenDate());
        out.setExpirationDate(input.getExpirationDate());
        out.setReissueDate(convertDateFormat(input.getReissueDate()));
        out.setCurrCycCredit(scaled(input.getCurrCycCredit()));
        out.setGroupId(input.getGroupId());

        // Business rule: if debit is zero, substitute with 2525.00;
        // otherwise use the actual debit value.
        // Note: the original COBOL only MOVEs 2525.00 when debit == 0 and never
        // explicitly moves the debit for the non-zero case (likely a bug).
        // The Java version implements the intended logic.
        if (input.getCurrCycDebit().compareTo(BigDecimal.ZERO) == 0) {
            out.setCurrCycDebit(DEBIT_SUBSTITUTION);
        } else {
            out.setCurrCycDebit(scaled(input.getCurrCycDebit()));
        }

        return out;
    }

    /**
     * Populates the array record (ARRYFILE).
     * Mirrors COBOL paragraph 1400-POPUL-ARRAY-RECORD.
     * The COBOL INITIALIZE zeroes the record first, then sets slots 1-3.
     */
    public ArrayRecord buildArrayRecord(AccountRecord input) {
        List<ArraySlot> slots = new ArrayList<>(5);
        BigDecimal balance = scaled(input.getCurrBal());

        slots.add(new ArraySlot(balance, SLOT1_DEBIT));
        slots.add(new ArraySlot(balance, SLOT2_DEBIT));
        slots.add(new ArraySlot(SLOT3_BALANCE, SLOT3_DEBIT));
        slots.add(new ArraySlot(BigDecimal.ZERO.setScale(SCALE, RoundingMode.DOWN),
                                BigDecimal.ZERO.setScale(SCALE, RoundingMode.DOWN)));
        slots.add(new ArraySlot(BigDecimal.ZERO.setScale(SCALE, RoundingMode.DOWN),
                                BigDecimal.ZERO.setScale(SCALE, RoundingMode.DOWN)));

        return new ArrayRecord(input.getAcctId(), slots);
    }

    /**
     * Populates the short variable-block record (VB1).
     * Mirrors COBOL paragraph 1500-POPUL-VBRC-RECORD (first half).
     */
    public VbRecord1 buildVbRecord1(AccountRecord input) {
        return new VbRecord1(input.getAcctId(), input.getActiveStatus());
    }

    /**
     * Populates the longer variable-block record (VB2).
     * Mirrors COBOL paragraph 1500-POPUL-VBRC-RECORD (second half).
     * The reissue year is extracted from the first 4 characters of the reissue date
     * (WS-ACCT-REISSUE-YYYY field populated by MOVE ACCT-REISSUE-DATE TO WS-REISSUE-DATE).
     */
    public VbRecord2 buildVbRecord2(AccountRecord input) {
        String reissueYear = extractReissueYear(input.getReissueDate());
        return new VbRecord2(
                input.getAcctId(),
                scaled(input.getCurrBal()),
                scaled(input.getCreditLimit()),
                reissueYear
        );
    }

    /**
     * Converts a date from YYYY-MM-DD to YYYYMMDD format.
     * Replaces the COBDATFT assembler call used in COBOL
     * (CODATECN-TYPE='2' input, CODATECN-OUTTYPE='2' output).
     *
     * @param yyyyMmDd date string in YYYY-MM-DD format
     * @return date string in YYYYMMDD format
     */
    public String convertDateFormat(String yyyyMmDd) {
        if (yyyyMmDd == null) {
            return "";
        }
        return yyyyMmDd.replace("-", "");
    }

    /**
     * Extracts the 4-character year from a YYYY-MM-DD date string.
     */
    public String extractReissueYear(String reissueDate) {
        if (reissueDate == null || reissueDate.length() < 4) {
            return "";
        }
        return reissueDate.substring(0, 4);
    }

    /**
     * Ensures a BigDecimal has scale 2 with RoundingMode.DOWN (truncation),
     * matching COBOL fixed-point PIC S9(10)V99 semantics.
     */
    private BigDecimal scaled(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.DOWN);
        }
        return value.setScale(SCALE, RoundingMode.DOWN);
    }

    /**
     * Container for the three output record sets produced by processing.
     */
    public static class ProcessingResult {
        private final List<OutAccountRecord> outRecords;
        private final List<ArrayRecord> arrayRecords;
        private final List<VbRecord1> vbRecords1;
        private final List<VbRecord2> vbRecords2;

        public ProcessingResult(List<OutAccountRecord> outRecords,
                                List<ArrayRecord> arrayRecords,
                                List<VbRecord1> vbRecords1,
                                List<VbRecord2> vbRecords2) {
            this.outRecords = outRecords;
            this.arrayRecords = arrayRecords;
            this.vbRecords1 = vbRecords1;
            this.vbRecords2 = vbRecords2;
        }

        public List<OutAccountRecord> getOutRecords() { return outRecords; }
        public List<ArrayRecord> getArrayRecords() { return arrayRecords; }
        public List<VbRecord1> getVbRecords1() { return vbRecords1; }
        public List<VbRecord2> getVbRecords2() { return vbRecords2; }
    }
}
