package com.carddemo.batch.cbact01c;

/**
 * Raised where CBACT01C calls {@code CEE3ABD} (paragraph {@code 9999-ABEND-PROGRAM}), or where the
 * original program would fail with a data exception.
 */
public final class AbendException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /** The user abend code CBACT01C passes to {@code CEE3ABD}. */
  public static final String USER_ABEND = "U0999";

  /** The system abend code for invalid numeric data. */
  public static final String DATA_EXCEPTION = "S0C7";

  private final String abendCode;
  private final String fileStatus;

  /**
   * Creates the exception.
   *
   * @param abendCode {@link #USER_ABEND} or {@link #DATA_EXCEPTION}
   * @param fileStatus the file status that caused the abend, or blank
   * @param message the reason
   * @param cause the underlying error, may be null
   */
  public AbendException(String abendCode, String fileStatus, String message, Throwable cause) {
    super(abendCode + ": " + message, cause);
    this.abendCode = abendCode;
    this.fileStatus = fileStatus;
  }

  /** Returns the abend code. */
  public String abendCode() {
    return abendCode;
  }

  /** Returns the file status that caused the abend, or blank. */
  public String fileStatus() {
    return fileStatus;
  }
}
