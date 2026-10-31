package com.carddemo.batch.cbact01c;

import java.nio.charset.StandardCharsets;

/** A logical record of {@code VBRCFILE} ({@code RECFM=VB}, record length 10 to 80). */
public sealed interface VariableRecord
    permits VariableRecord.StatusRecord, VariableRecord.BalanceRecord {

  /** Returns the record bytes; their count is the record length. */
  byte[] toBytes();

  /** Returns the record as the text {@code DISPLAY} prints. */
  default String text() {
    return new String(toBytes(), StandardCharsets.ISO_8859_1);
  }

  /**
   * {@code VBRC-REC1}, written with {@code WS-RECD-LEN} 12.
   *
   * @param accountId {@code VB1-ACCT-ID PIC 9(11)}
   * @param activeStatus {@code VB1-ACCT-ACTIVE-STATUS PIC X(01)}
   */
  record StatusRecord(String accountId, String activeStatus) implements VariableRecord {

    /** Length of the logical record. */
    public static final int LENGTH = 12;

    @Override
    public byte[] toBytes() {
      return new RecordBuffer(LENGTH).text(accountId, 11).text(activeStatus, 1).toBytes();
    }
  }

  /**
   * {@code VBRC-REC2}, written with {@code WS-RECD-LEN} 39.
   *
   * @param accountId {@code VB2-ACCT-ID PIC 9(11)}
   * @param currentBalance {@code VB2-ACCT-CURR-BAL PIC S9(10)V99}
   * @param creditLimit {@code VB2-ACCT-CREDIT-LIMIT PIC S9(10)V99}
   * @param reissueYear {@code VB2-ACCT-REISSUE-YYYY PIC X(04)}
   */
  record BalanceRecord(
      String accountId, SignedAmount currentBalance, SignedAmount creditLimit, String reissueYear)
      implements VariableRecord {

    /** Length of the logical record. */
    public static final int LENGTH = 39;

    @Override
    public byte[] toBytes() {
      return new RecordBuffer(LENGTH)
          .text(accountId, 11)
          .zoned(currentBalance, Account.AMOUNT_DIGITS, Account.AMOUNT_SCALE)
          .zoned(creditLimit, Account.AMOUNT_DIGITS, Account.AMOUNT_SCALE)
          .text(reissueYear, 4)
          .toBytes();
    }
  }
}
