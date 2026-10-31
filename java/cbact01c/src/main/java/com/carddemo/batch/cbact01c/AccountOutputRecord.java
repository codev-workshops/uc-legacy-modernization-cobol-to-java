package com.carddemo.batch.cbact01c;

/**
 * {@code OUT-ACCT-REC}, one record of {@code OUTFILE} ({@code RECFM=FB, LRECL=107}).
 *
 * @param accountId {@code OUT-ACCT-ID PIC 9(11)}
 * @param activeStatus {@code OUT-ACCT-ACTIVE-STATUS PIC X(01)}
 * @param currentBalance {@code OUT-ACCT-CURR-BAL PIC S9(10)V99}
 * @param creditLimit {@code OUT-ACCT-CREDIT-LIMIT PIC S9(10)V99}
 * @param cashCreditLimit {@code OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99}
 * @param openDate {@code OUT-ACCT-OPEN-DATE PIC X(10)}
 * @param expirationDate {@code OUT-ACCT-EXPIRAION-DATE PIC X(10)}
 * @param reissueDate {@code OUT-ACCT-REISSUE-DATE PIC X(10)}, reformatted by {@code COBDATFT}
 * @param currentCycleCredit {@code OUT-ACCT-CURR-CYC-CREDIT PIC S9(10)V99}
 * @param currentCycleDebit {@code OUT-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 COMP-3}
 * @param groupId {@code OUT-ACCT-GROUP-ID PIC X(10)}
 */
public record AccountOutputRecord(
    String accountId,
    String activeStatus,
    SignedAmount currentBalance,
    SignedAmount creditLimit,
    SignedAmount cashCreditLimit,
    String openDate,
    String expirationDate,
    String reissueDate,
    SignedAmount currentCycleCredit,
    CycleDebit currentCycleDebit,
    String groupId) {

  /** {@code LRECL} of {@code OUTFILE}. */
  public static final int RECORD_LENGTH = 107;

  /** Returns the record in its file layout. */
  public byte[] toBytes() {
    int digits = Account.AMOUNT_DIGITS;
    int scale = Account.AMOUNT_SCALE;
    return new RecordBuffer(RECORD_LENGTH)
        .text(accountId, 11)
        .text(activeStatus, 1)
        .zoned(currentBalance, digits, scale)
        .zoned(creditLimit, digits, scale)
        .zoned(cashCreditLimit, digits, scale)
        .text(openDate, 10)
        .text(expirationDate, 10)
        .text(reissueDate, 10)
        .zoned(currentCycleCredit, digits, scale)
        .raw(currentCycleDebit.toBytes())
        .text(groupId, 10)
        .toBytes();
  }
}
