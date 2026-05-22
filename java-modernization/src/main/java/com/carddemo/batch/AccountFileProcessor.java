package com.carddemo.batch;

import com.carddemo.io.AccountFileReader;
import com.carddemo.io.ArrayRecordWriter;
import com.carddemo.io.OutputAccountWriter;
import com.carddemo.io.VariableLengthRecordWriter;
import com.carddemo.model.*;
import com.carddemo.util.DateConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Java 17 modernization of COBOL batch program CBACT01C.
 * <p>
 * Reads an indexed account file (VSAM KSDS equivalent) sequentially
 * and writes three output files:
 * <ol>
 *   <li>OUT-FILE — selected account fields with date reformatting</li>
 *   <li>ARRY-FILE — array-based records with hardcoded balance entries</li>
 *   <li>VBRC-FILE — two variable-length records per account</li>
 * </ol>
 *
 * Business rules preserved from COBOL:
 * <ul>
 *   <li>If currCycDebit is zero, it is replaced with 2525.00 in the output record</li>
 *   <li>Reissue date is converted from YYYY-MM-DD to YYYYMMDD via date converter</li>
 *   <li>Array indices 1-2 carry the account's current balance; index 3 uses fixed values</li>
 *   <li>Variable-length records carry account status and balance/credit summaries</li>
 * </ul>
 */
public class AccountFileProcessor {

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARR_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outputFile;
    private final Path arrayFile;
    private final Path vbrFile;

    public AccountFileProcessor(Path inputFile, Path outputFile, Path arrayFile, Path vbrFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.arrayFile = arrayFile;
        this.vbrFile = vbrFile;
    }

    public void process() throws IOException {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        try (AccountFileReader reader = new AccountFileReader(inputFile);
             OutputAccountWriter outWriter = new OutputAccountWriter(outputFile);
             ArrayRecordWriter arrWriter = new ArrayRecordWriter(arrayFile);
             VariableLengthRecordWriter vbrWriter = new VariableLengthRecordWriter(vbrFile)) {

            Optional<AccountRecord> optRecord;
            while ((optRecord = reader.readNext()).isPresent()) {
                AccountRecord acct = optRecord.get();

                displayAccountRecord(acct);

                OutputAccountRecord outRec = buildOutputRecord(acct);
                outWriter.write(outRec);

                ArrayRecord arrRec = buildArrayRecord(acct);
                arrWriter.write(arrRec);

                VariableLengthRecord1 vb1 = buildVbRecord1(acct);
                VariableLengthRecord2 vb2 = buildVbRecord2(acct);
                vbrWriter.writeRecord1(vb1);
                vbrWriter.writeRecord2(vb2);
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
    }

    OutputAccountRecord buildOutputRecord(AccountRecord acct) {
        String reissueDate = acct.reissueDate();
        String convertedReissueDate = DateConverter.convert(reissueDate, 2, 2);

        BigDecimal currCycDebit = acct.currCycDebit();
        if (currCycDebit.compareTo(BigDecimal.ZERO) == 0) {
            currCycDebit = DEFAULT_CYC_DEBIT;
        }

        return new OutputAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                convertedReissueDate,
                acct.currCycCredit(),
                currCycDebit,
                acct.groupId()
        );
    }

    ArrayRecord buildArrayRecord(AccountRecord acct) {
        List<ArrayRecord.BalanceEntry> entries = new ArrayList<>(5);

        // Index 1: account current balance + fixed debit 1005.00
        entries.add(new ArrayRecord.BalanceEntry(acct.currBal(), ARR_DEBIT_1));
        // Index 2: account current balance + fixed debit 1525.00
        entries.add(new ArrayRecord.BalanceEntry(acct.currBal(), ARR_DEBIT_2));
        // Index 3: fixed values -1025.00 and -2500.00
        entries.add(new ArrayRecord.BalanceEntry(ARR_BAL_3, ARR_DEBIT_3));
        // Indices 4-5: initialized to zeros (COBOL INITIALIZE)
        entries.add(new ArrayRecord.BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));
        entries.add(new ArrayRecord.BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));

        return new ArrayRecord(acct.acctId(), entries, "    ");
    }

    VariableLengthRecord1 buildVbRecord1(AccountRecord acct) {
        return new VariableLengthRecord1(acct.acctId(), acct.activeStatus());
    }

    VariableLengthRecord2 buildVbRecord2(AccountRecord acct) {
        String reissueYear = acct.reissueDate().substring(0, 4);
        return new VariableLengthRecord2(
                acct.acctId(),
                acct.currBal(),
                acct.creditLimit(),
                reissueYear
        );
    }

    private void displayAccountRecord(AccountRecord acct) {
        System.out.println("ACCT-ID                 :" + acct.acctId());
        System.out.println("ACCT-ACTIVE-STATUS      :" + acct.activeStatus());
        System.out.println("ACCT-CURR-BAL           :" + acct.currBal());
        System.out.println("ACCT-CREDIT-LIMIT       :" + acct.creditLimit());
        System.out.println("ACCT-CASH-CREDIT-LIMIT  :" + acct.cashCreditLimit());
        System.out.println("ACCT-OPEN-DATE          :" + acct.openDate());
        System.out.println("ACCT-EXPIRAION-DATE     :" + acct.expirationDate());
        System.out.println("ACCT-REISSUE-DATE       :" + acct.reissueDate());
        System.out.println("ACCT-CURR-CYC-CREDIT    :" + acct.currCycCredit());
        System.out.println("ACCT-CURR-CYC-DEBIT     :" + acct.currCycDebit());
        System.out.println("ACCT-GROUP-ID           :" + acct.groupId());
        System.out.println("-------------------------------------------------");
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: AccountFileProcessor <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3])
        );

        try {
            processor.process();
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM");
            System.err.println("FILE STATUS IS: " + e.getMessage());
            System.exit(999);
        }
    }
}
