package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.AccountArrayRecord.Balance;
import com.carddemo.batch.cbact01c.JobOptions.CycleDebitPolicy;
import com.carddemo.batch.cbact01c.VariableRecord.BalanceRecord;
import com.carddemo.batch.cbact01c.VariableRecord.StatusRecord;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Java 17 port of CBACT01C: reads every account in key order and writes, per account, one {@code
 * OUTFILE} record, one {@code ARRYFILE} record and two {@code VBRCFILE} records. The {@code
 * DISPLAY} output of the COBOL program is written to {@code sysout}.
 */
public final class AccountExtractJob {

  /** Value moved into {@code OUT-ACCT-CURR-CYC-DEBIT} when the account's cycle debit is zero. */
  static final SignedAmount DEFAULT_CYCLE_DEBIT = SignedAmount.of("2525.00");

  private static final List<Balance> CONSTANT_BALANCES_TAIL =
      List.of(
          new Balance(SignedAmount.of("-1025.00"), SignedAmount.of("-2500.00")),
          Balance.INITIAL,
          Balance.INITIAL);
  private static final SignedAmount FIRST_CYCLE_DEBIT = SignedAmount.of("1005.00");
  private static final SignedAmount SECOND_CYCLE_DEBIT = SignedAmount.of("1525.00");
  private static final String SEPARATOR = "-------------------------------------------------";

  private final JobOptions options;
  private final PrintStream sysout;

  /** {@code CODATECN-0UT-DATE}: working storage that keeps its contents between calls. */
  private String dateOutputBuffer = " ".repeat(DateFormatConverter.BUFFER_LENGTH);

  /** {@code OUT-ACCT-CURR-CYC-DEBIT} as the record area holds it between writes. */
  private CycleDebit cycleDebit = new CycleDebit.LowValues();

  /**
   * Creates a job.
   *
   * @param options run options
   * @param sysout where the program's {@code DISPLAY} output goes
   */
  public AccountExtractJob(JobOptions options, PrintStream sysout) {
    this.options = Objects.requireNonNull(options, "options");
    this.sysout = Objects.requireNonNull(sysout, "sysout");
  }

  /**
   * Runs the extract. A job instance runs once.
   *
   * @param datasets the DD paths
   * @return record counts
   * @throws AbendException where the COBOL program would abend
   */
  public Summary run(Datasets datasets) {
    sysout.println("START OF EXECUTION OF PROGRAM CBACT01C");
    AccountFile accounts = openInput(datasets.acctFile());
    try (FixedRecordWriter outFile =
            openFixed(datasets.outFile(), AccountOutputRecord.RECORD_LENGTH, "OUTFILE");
        FixedRecordWriter arrayFile =
            openFixed(datasets.arryFile(), AccountArrayRecord.RECORD_LENGTH, "ARRAYFILE");
        VariableRecordWriter vbFile = openVariable(datasets.vbrcFile())) {
      int count = 0;
      for (String raw : accounts) {
        Account account = parse(raw);
        displayAccount(account);
        write(outFile, toOutputRecord(account).toBytes());
        write(arrayFile, toArrayRecord(account).toBytes());
        StatusRecord statusRecord = new StatusRecord(account.id(), account.activeStatus());
        BalanceRecord balanceRecord =
            new BalanceRecord(
                account.id(),
                account.currentBalance(),
                account.creditLimit(),
                account.reissueDate().substring(0, 4));
        sysout.println("VBRC-REC1:" + statusRecord.text());
        sysout.println("VBRC-REC2:" + balanceRecord.text());
        write(vbFile, statusRecord);
        write(vbFile, balanceRecord);
        sysout.println(raw);
        count++;
      }
      sysout.println("END OF EXECUTION OF PROGRAM CBACT01C");
      return new Summary(count, count, count, count * 2);
    } catch (IOException e) {
      throw abend(FileStatus.forOutput(e), "error closing output file", e);
    }
  }

  /** Paragraph {@code 1300-POPUL-ACCT-RECORD}. */
  AccountOutputRecord toOutputRecord(Account account) {
    DateFormatConverter.Result converted =
        DateFormatConverter.convert('2', account.reissueDate(), '2', dateOutputBuffer);
    dateOutputBuffer = converted.outputDate();
    if (account.currentCycleDebit().isZero()) {
      cycleDebit = new CycleDebit.Value(DEFAULT_CYCLE_DEBIT);
    } else if (options.cycleDebitPolicy() == CycleDebitPolicy.COPY_SOURCE) {
      cycleDebit = new CycleDebit.Value(account.currentCycleDebit());
    }
    return new AccountOutputRecord(
        account.id(),
        account.activeStatus(),
        account.currentBalance(),
        account.creditLimit(),
        account.cashCreditLimit(),
        account.openDate(),
        account.expirationDate(),
        dateOutputBuffer.substring(0, 10),
        account.currentCycleCredit(),
        cycleDebit,
        account.groupId());
  }

  /** {@code INITIALIZE ARR-ARRAY-REC} then paragraph {@code 1400-POPUL-ARRAY-RECORD}. */
  static AccountArrayRecord toArrayRecord(Account account) {
    SignedAmount balance = account.currentBalance();
    List<Balance> balances =
        List.of(
            new Balance(balance, FIRST_CYCLE_DEBIT),
            new Balance(balance, SECOND_CYCLE_DEBIT),
            CONSTANT_BALANCES_TAIL.get(0),
            CONSTANT_BALANCES_TAIL.get(1),
            CONSTANT_BALANCES_TAIL.get(2));
    return new AccountArrayRecord(account.id(), balances);
  }

  private Account parse(String raw) {
    try {
      return Account.parse(raw);
    } catch (IllegalArgumentException e) {
      sysout.println("ABENDING PROGRAM");
      throw new AbendException(AbendException.DATA_EXCEPTION, "", e.getMessage(), e);
    }
  }

  /** Paragraph {@code 1100-DISPLAY-ACCT-RECORD}. */
  private void displayAccount(Account account) {
    display("ACCT-ID                 :", account.id());
    display("ACCT-ACTIVE-STATUS      :", account.activeStatus());
    display("ACCT-CURR-BAL           :", account.currentBalance());
    display("ACCT-CREDIT-LIMIT       :", account.creditLimit());
    display("ACCT-CASH-CREDIT-LIMIT  :", account.cashCreditLimit());
    display("ACCT-OPEN-DATE          :", account.openDate());
    display("ACCT-EXPIRAION-DATE     :", account.expirationDate());
    display("ACCT-REISSUE-DATE       :", account.reissueDate());
    display("ACCT-CURR-CYC-CREDIT    :", account.currentCycleCredit());
    display("ACCT-CURR-CYC-DEBIT     :", account.currentCycleDebit());
    display("ACCT-GROUP-ID           :", account.groupId());
    sysout.println(SEPARATOR);
  }

  private void display(String label, String value) {
    sysout.println(label + value);
  }

  private void display(String label, SignedAmount value) {
    display(label, ZonedDecimal.encode(value, Account.AMOUNT_DIGITS, Account.AMOUNT_SCALE));
  }

  /** Paragraph {@code 0000-ACCTFILE-OPEN}. */
  private AccountFile openInput(Path path) {
    try {
      return AccountFile.open(path);
    } catch (AccountFile.InvalidAccountFileException e) {
      sysout.println("ERROR OPENING ACCTFILE");
      throw abend(FileStatus.ATTRIBUTE_CONFLICT, e.getMessage(), e);
    } catch (IOException e) {
      sysout.println("ERROR OPENING ACCTFILE");
      throw abend(FileStatus.forInputOpen(e), "cannot open ACCTFILE " + path, e);
    }
  }

  /** Paragraphs {@code 2000-OUTFILE-OPEN} and {@code 3000-ARRFILE-OPEN}. */
  private FixedRecordWriter openFixed(Path path, int recordLength, String name) {
    try {
      return new FixedRecordWriter(path, recordLength);
    } catch (IOException e) {
      String status = FileStatus.forOutput(e);
      sysout.println("ERROR OPENING " + name + status);
      throw abend(status, "cannot open " + name + " " + path, e);
    }
  }

  /** Paragraph {@code 4000-VBRFILE-OPEN}. */
  private VariableRecordWriter openVariable(Path path) {
    try {
      return new VariableRecordWriter(path, options.rdwFormat());
    } catch (IOException e) {
      String status = FileStatus.forOutput(e);
      sysout.println("ERROR OPENING VBRC FILE" + status);
      throw abend(status, "cannot open VBRCFILE " + path, e);
    }
  }

  /** Paragraphs {@code 1350-WRITE-ACCT-RECORD} and {@code 1450-WRITE-ARRY-RECORD}. */
  private void write(FixedRecordWriter writer, byte[] record) {
    try {
      writer.write(record);
    } catch (IOException e) {
      throw writeAbend(e);
    }
  }

  /** Paragraphs {@code 1550-WRITE-VB1-RECORD} and {@code 1575-WRITE-VB2-RECORD}. */
  private void write(VariableRecordWriter writer, VariableRecord record) {
    try {
      writer.write(record);
    } catch (IOException e) {
      throw writeAbend(e);
    }
  }

  private AbendException writeAbend(IOException e) {
    String status = FileStatus.forOutput(e);
    sysout.println("ACCOUNT FILE WRITE STATUS IS:" + status);
    return abend(status, "write failed", e);
  }

  /** Paragraphs {@code 9910-DISPLAY-IO-STATUS} and {@code 9999-ABEND-PROGRAM}. */
  private AbendException abend(String status, String message, Exception cause) {
    sysout.println("FILE STATUS IS: NNNN" + FileStatus.format(status));
    sysout.println("ABENDING PROGRAM");
    return new AbendException(AbendException.USER_ABEND, status, message, cause);
  }

  /**
   * The DD statements of step {@code STEP05} in {@code READACCT.jcl}.
   *
   * @param acctFile {@code ACCTFILE}: account KSDS unload (input)
   * @param outFile {@code OUTFILE}: {@code RECFM=FB, LRECL=107}
   * @param arryFile {@code ARRYFILE}: {@code RECFM=FB, LRECL=110}
   * @param vbrcFile {@code VBRCFILE}: {@code RECFM=VB, LRECL=84}
   */
  public record Datasets(Path acctFile, Path outFile, Path arryFile, Path vbrcFile) {}

  /**
   * Record counts of one run.
   *
   * @param accountsRead records read from {@code ACCTFILE}
   * @param outRecords records written to {@code OUTFILE}
   * @param arrayRecords records written to {@code ARRYFILE}
   * @param variableRecords records written to {@code VBRCFILE}
   */
  public record Summary(int accountsRead, int outRecords, int arrayRecords, int variableRecords) {}
}
