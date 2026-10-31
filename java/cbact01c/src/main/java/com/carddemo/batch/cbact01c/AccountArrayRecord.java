package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;
import java.util.List;

/**
 * {@code ARR-ARRAY-REC}, one record of {@code ARRYFILE} ({@code RECFM=FB, LRECL=110}).
 *
 * @param accountId {@code ARR-ACCT-ID PIC 9(11)}
 * @param balances the five {@code ARR-ACCT-BAL OCCURS 5 TIMES} entries
 */
public record AccountArrayRecord(String accountId, List<Balance> balances) {

  /** {@code LRECL} of {@code ARRYFILE}. */
  public static final int RECORD_LENGTH = 110;

  /** Number of {@code ARR-ACCT-BAL} occurrences. */
  public static final int OCCURS = 5;

  private static final int FILLER_LENGTH = 4;

  /** Validates the number of occurrences and makes the list unmodifiable. */
  public AccountArrayRecord {
    if (balances.size() != OCCURS) {
      throw new IllegalArgumentException("expected " + OCCURS + " balances: " + balances);
    }
    balances = List.copyOf(balances);
  }

  /** Returns the record in its file layout; {@code ARR-FILLER} is spaces. */
  public byte[] toBytes() {
    RecordBuffer buffer = new RecordBuffer(RECORD_LENGTH).text(accountId, 11);
    for (Balance balance : balances) {
      buffer
          .zoned(balance.currentBalance(), Account.AMOUNT_DIGITS, Account.AMOUNT_SCALE)
          .packed(balance.currentCycleDebit(), Account.AMOUNT_DIGITS, Account.AMOUNT_SCALE);
    }
    return buffer.text("", FILLER_LENGTH).toBytes();
  }

  /**
   * One {@code ARR-ACCT-BAL} entry.
   *
   * @param currentBalance {@code ARR-ACCT-CURR-BAL PIC S9(10)V99}
   * @param currentCycleDebit {@code ARR-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 COMP-3}
   */
  public record Balance(SignedAmount currentBalance, BigDecimal currentCycleDebit) {

    /** An entry after {@code INITIALIZE}: both amounts zero. */
    public static final Balance INITIAL = new Balance(SignedAmount.ZERO, BigDecimal.ZERO);
  }
}
