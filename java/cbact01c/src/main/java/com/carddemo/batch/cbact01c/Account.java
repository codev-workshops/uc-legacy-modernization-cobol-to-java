package com.carddemo.batch.cbact01c;

/**
 * One account master record, copybook {@code CVACT01Y} ({@code ACCOUNT-RECORD}, 300 bytes).
 *
 * @param id {@code ACCT-ID PIC 9(11)}, the KSDS key
 * @param activeStatus {@code ACCT-ACTIVE-STATUS PIC X(01)}
 * @param currentBalance {@code ACCT-CURR-BAL PIC S9(10)V99}
 * @param creditLimit {@code ACCT-CREDIT-LIMIT PIC S9(10)V99}
 * @param cashCreditLimit {@code ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99}
 * @param openDate {@code ACCT-OPEN-DATE PIC X(10)}, {@code YYYY-MM-DD}
 * @param expirationDate {@code ACCT-EXPIRAION-DATE PIC X(10)}, {@code YYYY-MM-DD}
 * @param reissueDate {@code ACCT-REISSUE-DATE PIC X(10)}, {@code YYYY-MM-DD}
 * @param currentCycleCredit {@code ACCT-CURR-CYC-CREDIT PIC S9(10)V99}
 * @param currentCycleDebit {@code ACCT-CURR-CYC-DEBIT PIC S9(10)V99}
 * @param addressZip {@code ACCT-ADDR-ZIP PIC X(10)}
 * @param groupId {@code ACCT-GROUP-ID PIC X(10)}
 */
public record Account(
    String id,
    String activeStatus,
    SignedAmount currentBalance,
    SignedAmount creditLimit,
    SignedAmount cashCreditLimit,
    String openDate,
    String expirationDate,
    String reissueDate,
    SignedAmount currentCycleCredit,
    SignedAmount currentCycleDebit,
    String addressZip,
    String groupId) {

  /** Record length of {@code ACCOUNT-RECORD} and of the KSDS. */
  public static final int RECORD_LENGTH = 300;

  /** Length of the {@code ACCT-ID} key. */
  public static final int KEY_LENGTH = 11;

  /** Digits in every {@code S9(10)V99} amount field. */
  static final int AMOUNT_DIGITS = 12;

  /** Implied decimal places in every amount field. */
  static final int AMOUNT_SCALE = 2;

  /**
   * Parses a 300-character account record.
   *
   * @param record the record text
   * @return the account
   * @throws IllegalArgumentException if the record has the wrong length or invalid numeric data
   */
  public static Account parse(String record) {
    if (record.length() != RECORD_LENGTH) {
      throw new IllegalArgumentException(
          "account record is " + record.length() + " bytes, expected " + RECORD_LENGTH);
    }
    Fields fields = new Fields(record);
    String id = fields.text(KEY_LENGTH);
    if (!id.chars().allMatch(c -> c >= '0' && c <= '9')) {
      throw new IllegalArgumentException("invalid ACCT-ID '" + id + "'");
    }
    return new Account(
        id,
        fields.text(1),
        fields.amount(),
        fields.amount(),
        fields.amount(),
        fields.text(10),
        fields.text(10),
        fields.text(10),
        fields.amount(),
        fields.amount(),
        fields.text(10),
        fields.text(10));
  }

  /** Sequential reader over the fields of one record. */
  private static final class Fields {
    private final String record;
    private int offset;

    Fields(String record) {
      this.record = record;
    }

    String text(int width) {
      String value = record.substring(offset, offset + width);
      offset += width;
      return value;
    }

    SignedAmount amount() {
      int start = offset;
      String field = text(AMOUNT_DIGITS);
      try {
        return ZonedDecimal.decode(field, AMOUNT_SCALE);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "invalid numeric data at offset " + start + " of account " + record.substring(0, 11),
            e);
      }
    }
  }
}
