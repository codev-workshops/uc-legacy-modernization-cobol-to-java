package com.carddemo.batch.cbact01c;

/**
 * Port of the assembler subroutine {@code COBDATFT} called through copybook {@code CODATECN}.
 *
 * <p>Input type {@code '1'} ({@code YYYYMMDD}) converts to output type {@code '1'} ({@code
 * YYYY-MM-DD}); input type {@code '2'} ({@code YYYY-MM-DD}) converts to output type {@code '2'}
 * ({@code YYYYMMDD}). The routine only moves characters: it does not check that the input is a real
 * date. It writes 10 or 8 characters and leaves the rest of the 20-character output buffer as the
 * caller left it.
 */
public final class DateFormatConverter {

  /** Length of {@code CODATECN-INP-DATE} and {@code CODATECN-0UT-DATE}. */
  public static final int BUFFER_LENGTH = 20;

  /** Length of {@code CODATECN-ERROR-MSG}. */
  public static final int ERROR_LENGTH = 38;

  /** Text {@code COBDATFT} moves into the error message on bad input. */
  public static final String INVALID_INPUT = "INVALID INPUT";

  private DateFormatConverter() {}

  /**
   * Converts a date the way {@code CALL 'COBDATFT' USING CODATECN-REC} does.
   *
   * @param inputType {@code CODATECN-TYPE}
   * @param inputDate {@code CODATECN-INP-DATE}
   * @param outputType {@code CODATECN-OUTTYPE}
   * @param outputDate the current contents of {@code CODATECN-0UT-DATE}
   * @return the new output buffer, or the unchanged buffer and an error message
   */
  public static Result convert(
      char inputType, String inputDate, char outputType, String outputDate) {
    String in = Alphanumeric.fit(inputDate, BUFFER_LENGTH);
    String out = Alphanumeric.fit(outputDate, BUFFER_LENGTH);
    if (inputType == '1' && in.charAt(4) != '-' && outputType != '2') {
      String iso = in.substring(0, 4) + '-' + in.substring(4, 6) + '-' + in.substring(6, 8);
      return Result.converted(iso + out.substring(10));
    }
    if (inputType == '2' && outputType != '1') {
      String compact = in.substring(0, 4) + in.substring(5, 7) + in.substring(8, 10);
      return Result.converted(compact + out.substring(8));
    }
    return new Result(out, Alphanumeric.fit(INVALID_INPUT, ERROR_LENGTH));
  }

  /**
   * Result of a conversion.
   *
   * @param outputDate the 20-character {@code CODATECN-0UT-DATE} after the call
   * @param errorMessage {@code CODATECN-ERROR-MSG}, blank when the conversion succeeded
   */
  public record Result(String outputDate, String errorMessage) {

    static Result converted(String outputDate) {
      return new Result(outputDate, " ".repeat(ERROR_LENGTH));
    }

    /** Returns whether {@code COBDATFT} rejected the input. */
    public boolean isError() {
      return !errorMessage.isBlank();
    }
  }
}
