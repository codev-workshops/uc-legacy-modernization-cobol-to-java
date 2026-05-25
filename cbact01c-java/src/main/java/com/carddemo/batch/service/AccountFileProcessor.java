package com.carddemo.batch.service;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.ArrayRecord.BalanceEntry;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VariableLengthRecord;
import com.carddemo.batch.util.DateFormatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Java equivalent of COBOL batch program CBACT01C.
 *
 * Business logic:
 * 1. Opens an indexed account file (ACCTFILE) for sequential read.
 * 2. For each account record:
 *    a. Displays the record fields (logging).
 *    b. Populates an output account record with date reformatting via COBDATFT.
 *    c. If current cycle debit is zero, substitutes 2525.00.
 *    d. Writes the output record to OUT-FILE.
 *    e. Populates an array record with balance entries (3 of 5 slots filled).
 *    f. Writes the array record to ARRY-FILE.
 *    g. Populates and writes two variable-length records to VBRC-FILE.
 * 3. Closes all files.
 */
public class AccountFileProcessor {

    private static final Logger LOG = Logger.getLogger(AccountFileProcessor.class.getName());

    private static final BigDecimal DEFAULT_CYCLE_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outputFile;
    private final Path arrayFile;
    private final Path vbrcFile;

    public AccountFileProcessor(Path inputFile, Path outputFile, Path arrayFile, Path vbrcFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.arrayFile = arrayFile;
        this.vbrcFile = vbrcFile;
    }

    /**
     * Executes the batch processing, reading accounts and writing output files.
     *
     * @return the number of records processed
     * @throws IOException if any file I/O error occurs
     */
    public int process() throws IOException {
        LOG.info("START OF EXECUTION OF PROGRAM CBACT01C");

        int recordCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(inputFile);
             BufferedWriter outWriter = Files.newBufferedWriter(outputFile);
             BufferedWriter arrWriter = Files.newBufferedWriter(arrayFile);
             BufferedWriter vbrWriter = Files.newBufferedWriter(vbrcFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                AccountRecord account = AccountRecord.fromFixedLength(line);
                displayAccountRecord(account);

                OutputAccountRecord outRecord = populateOutputRecord(account);
                outWriter.write(outRecord.toDelimitedString());
                outWriter.newLine();

                ArrayRecord arrRecord = populateArrayRecord(account);
                arrWriter.write(arrRecord.toDelimitedString());
                arrWriter.newLine();

                List<VariableLengthRecord> vbrRecords = populateVbrcRecords(account);
                for (VariableLengthRecord vbr : vbrRecords) {
                    if (vbr instanceof VariableLengthRecord.ShortRecord sr) {
                        vbrWriter.write(sr.toDelimitedString());
                    } else if (vbr instanceof VariableLengthRecord.LongRecord lr) {
                        vbrWriter.write(lr.toDelimitedString());
                    }
                    vbrWriter.newLine();
                }

                recordCount++;
            }
        }

        LOG.info("END OF EXECUTION OF PROGRAM CBACT01C - Records processed: " + recordCount);
        return recordCount;
    }

    private void displayAccountRecord(AccountRecord account) {
        LOG.fine(() -> String.format("""
                ACCT-ID                 :%011d
                ACCT-ACTIVE-STATUS      :%s
                ACCT-CURR-BAL           :%s
                ACCT-CREDIT-LIMIT       :%s
                ACCT-CASH-CREDIT-LIMIT  :%s
                ACCT-OPEN-DATE          :%s
                ACCT-EXPIRAION-DATE     :%s
                ACCT-REISSUE-DATE       :%s
                ACCT-CURR-CYC-CREDIT    :%s
                ACCT-CURR-CYC-DEBIT     :%s
                ACCT-GROUP-ID           :%s
                -------------------------------------------------""",
                account.acctId(), account.activeStatus(),
                account.currentBalance(), account.creditLimit(),
                account.cashCreditLimit(), account.openDate(),
                account.expirationDate(), account.reissueDate(),
                account.currentCycleCredit(), account.currentCycleDebit(),
                account.groupId()));
    }

    /**
     * Populates the output account record (paragraph 1300-POPUL-ACCT-RECORD).
     * Calls DateFormatter (replacing COBDATFT) with type='2' input and output='2',
     * converting YYYY-MM-DD to YYYYMMDD.
     * If current cycle debit is zero, substitutes 2525.00.
     */
    OutputAccountRecord populateOutputRecord(AccountRecord account) {
        String formattedReissueDate = DateFormatter.formatDate(
                account.reissueDate(), "2", "2");

        BigDecimal cycleDebit = account.currentCycleDebit().compareTo(BigDecimal.ZERO) == 0
                ? DEFAULT_CYCLE_DEBIT
                : account.currentCycleDebit();

        return new OutputAccountRecord(
                account.acctId(),
                account.activeStatus(),
                account.currentBalance(),
                account.creditLimit(),
                account.cashCreditLimit(),
                account.openDate(),
                account.expirationDate(),
                formattedReissueDate,
                account.currentCycleCredit(),
                cycleDebit,
                account.groupId()
        );
    }

    /**
     * Populates the array record (paragraph 1400-POPUL-ARRAY-RECORD).
     * Fills 3 of 5 balance entries:
     *   [0] = (account balance, 1005.00)
     *   [1] = (account balance, 1525.00)
     *   [2] = (-1025.00, -2500.00)
     *   [3] = (0, 0) - initialized
     *   [4] = (0, 0) - initialized
     */
    ArrayRecord populateArrayRecord(AccountRecord account) {
        List<BalanceEntry> entries = new ArrayList<>(5);
        entries.add(new BalanceEntry(account.currentBalance(), ARRAY_DEBIT_1));
        entries.add(new BalanceEntry(account.currentBalance(), ARRAY_DEBIT_2));
        entries.add(new BalanceEntry(ARRAY_BAL_3, ARRAY_DEBIT_3));
        entries.add(BalanceEntry.zero());
        entries.add(BalanceEntry.zero());
        return new ArrayRecord(account.acctId(), entries);
    }

    /**
     * Populates variable-length records (paragraph 1500-POPUL-VBRC-RECORD).
     * Returns a short record (ID + status) and a long record (ID + balance + limit + year).
     */
    List<VariableLengthRecord> populateVbrcRecords(AccountRecord account) {
        String reissueYear = account.reissueDate().length() >= 4
                ? account.reissueDate().substring(0, 4)
                : "";

        var shortRec = new VariableLengthRecord.ShortRecord(
                account.acctId(), account.activeStatus());

        var longRec = new VariableLengthRecord.LongRecord(
                account.acctId(), account.currentBalance(),
                account.creditLimit(), reissueYear);

        return List.of(shortRec, longRec);
    }
}
