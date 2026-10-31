package com.carddemo.batch.cbact01c;

import java.math.BigDecimal;

/**
 * Contents of {@code OUT-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 COMP-3}.
 *
 * <p>CBACT01C only moves a value into this field when the account's cycle debit is zero, so for
 * other accounts the field keeps whatever the record area last held: either an earlier value or,
 * before any value was moved, the low-values the file buffer starts with.
 */
public sealed interface CycleDebit permits CycleDebit.Value, CycleDebit.LowValues {

  /** Bytes of the {@code COMP-3} field. */
  int LENGTH = PackedDecimal.byteLength(Account.AMOUNT_DIGITS);

  /** Returns the field bytes. */
  byte[] toBytes();

  /**
   * A value moved into the field.
   *
   * @param amount the packed value
   */
  record Value(BigDecimal amount) implements CycleDebit {
    @Override
    public byte[] toBytes() {
      return PackedDecimal.encode(amount, Account.AMOUNT_DIGITS, Account.AMOUNT_SCALE);
    }
  }

  /** The field has never been set: all bytes are {@code X'00'}. */
  record LowValues() implements CycleDebit {
    @Override
    public byte[] toBytes() {
      return new byte[LENGTH];
    }
  }
}
