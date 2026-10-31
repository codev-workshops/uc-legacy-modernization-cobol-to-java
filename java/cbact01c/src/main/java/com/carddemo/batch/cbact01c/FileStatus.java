package com.carddemo.batch.cbact01c;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;

/** COBOL file status codes used by CBACT01C and the {@code 9910-DISPLAY-IO-STATUS} format. */
public final class FileStatus {

  /** Successful completion. */
  public static final String OK = "00";

  /** Permanent I/O error. */
  public static final String PERMANENT_ERROR = "30";

  /** {@code OPEN} of a file that does not exist. */
  public static final String FILE_NOT_FOUND = "35";

  /** {@code OPEN} not allowed on the file (for example it is a directory or read-only). */
  public static final String OPEN_NOT_PERMITTED = "37";

  /** The file's attributes conflict with the program's (for example the record length). */
  public static final String ATTRIBUTE_CONFLICT = "39";

  private FileStatus() {}

  /** Maps the failure to open an input file to its file status. */
  static String forInputOpen(IOException e) {
    return e instanceof NoSuchFileException ? FILE_NOT_FOUND : forOutput(e);
  }

  /** Maps a failure to open or write an output file to its file status. */
  static String forOutput(IOException e) {
    if (e instanceof AccessDeniedException) {
      return OPEN_NOT_PERMITTED;
    }
    if (e instanceof FileSystemException fse && fse.getReason() != null) {
      return fse.getReason().contains("directory") ? OPEN_NOT_PERMITTED : PERMANENT_ERROR;
    }
    return PERMANENT_ERROR;
  }

  /**
   * Formats a status like paragraph {@code 9910-DISPLAY-IO-STATUS}: {@code "00"} plus the status
   * when it is numeric and not a {@code 9x} code; otherwise the first character followed by the
   * second byte as a three-digit binary value (VSAM extended status).
   *
   * @param status the two-character file status
   * @return the {@code IO-STATUS-04} text
   */
  public static String format(String status) {
    String twoChars = Alphanumeric.fit(status, 2);
    boolean numeric = twoChars.chars().allMatch(c -> c >= '0' && c <= '9');
    if (!numeric || twoChars.charAt(0) == '9') {
      return twoChars.charAt(0) + String.format("%03d", (twoChars.charAt(1) & 0xFF) % 1000);
    }
    return "00" + twoChars;
  }
}
