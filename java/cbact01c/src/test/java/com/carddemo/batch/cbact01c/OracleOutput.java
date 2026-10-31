package com.carddemo.batch.cbact01c;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Output of one run of the unmodified CBACT01C under GnuCOBOL ({@code src/test/cobol}), with the
 * two documented GnuCOBOL-versus-z/OS differences mapped to the z/OS form. See MIGRATION_NOTES.md,
 * section "Platform differences in the oracle".
 */
record OracleOutput(byte[] outFile, byte[] arryFile, byte[] vbrcFile, String sysout, int rc) {

  static final Path GOLDEN = Path.of("src/test/resources/golden");
  static final Path SAMPLE_INPUT = Path.of("../../app/data/ASCII/acctdata.txt");
  static final Path INPUT = Path.of("src/test/resources/input");

  /** Offsets in an ARRYFILE record of the last byte of ARR-ACCT-CURR-BAL(4) and (5). */
  static final int[] INITIALIZED_BALANCE_SIGN_OFFSETS = {11 + 3 * 19 + 11, 11 + 4 * 19 + 11};

  private static final Pattern GNUCOBOL_SIGNED_DISPLAY =
      Pattern.compile("^(ACCT-[A-Z-]+ +:)(\\d{11})(\\d)([+-])$");

  static OracleOutput load(String scenario) {
    return load(GOLDEN.resolve(scenario));
  }

  static OracleOutput load(Path dir) {
    try {
      return new OracleOutput(
          readIfPresent(dir.resolve("OUTFILE")),
          readIfPresent(dir.resolve("ARRYFILE")),
          readIfPresent(dir.resolve("VBRCFILE")),
          Files.readString(dir.resolve("SYSOUT"), StandardCharsets.ISO_8859_1),
          Integer.parseInt(Files.readString(dir.resolve("RC")).trim()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** An output the program abended before opening is read as empty. */
  private static byte[] readIfPresent(Path path) throws IOException {
    return Files.exists(path) ? Files.readAllBytes(path) : new byte[0];
  }

  /**
   * GnuCOBOL leaves {@code INITIALIZE}d signed DISPLAY zeros unsigned ({@code '0'}); Enterprise
   * COBOL stores the preferred positive sign ({@code X'C0'}, {@code '&#123;'} in the ASCII
   * rendition).
   */
  byte[] arryFileAsZos() {
    byte[] copy = arryFile.clone();
    for (int base = 0; base < copy.length; base += AccountArrayRecord.RECORD_LENGTH) {
      for (int offset : INITIALIZED_BALANCE_SIGN_OFFSETS) {
        if (copy[base + offset] == '0') {
          copy[base + offset] = '{';
        }
      }
    }
    return copy;
  }

  /**
   * GnuCOBOL prints a signed DISPLAY field as digits plus a trailing sign; Enterprise COBOL prints
   * the field's bytes (overpunched last digit). The {@code CEE3ABD} stub's trace line is dropped.
   */
  String sysoutAsZos() {
    return sysout
        .lines()
        .filter(line -> !line.startsWith("CEE3ABD "))
        .map(OracleOutput::overpunch)
        .collect(Collectors.joining("\n", "", "\n"));
  }

  private static String overpunch(String line) {
    Matcher m = GNUCOBOL_SIGNED_DISPLAY.matcher(line);
    if (!m.matches()) {
      return line;
    }
    int digit = m.group(3).charAt(0) - '0';
    char sign = (m.group(4).equals("-") ? "}JKLMNOPQR" : "{ABCDEFGHI").charAt(digit);
    return m.group(1) + m.group(2) + sign;
  }

  /** Splits a GnuCOBOL variable-length file into its logical records. */
  static List<byte[]> logicalRecords(byte[] file, RdwFormat format) {
    List<byte[]> records = new ArrayList<>();
    int pos = 0;
    while (pos < file.length) {
      byte[] prefix = Arrays.copyOfRange(file, pos, pos + RdwFormat.PREFIX_LENGTH);
      int length = format.dataLength(prefix);
      pos += RdwFormat.PREFIX_LENGTH;
      records.add(Arrays.copyOfRange(file, pos, pos + length));
      pos += length;
    }
    return records;
  }
}
