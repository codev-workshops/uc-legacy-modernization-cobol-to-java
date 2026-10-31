package com.carddemo.batch.cbact01c;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The account KSDS ({@code ACCTFILE}) read sequentially in key order.
 *
 * <p>The Java version reads an ASCII unload of the KSDS: one 300-byte record per line, as in {@code
 * app/data/ASCII/acctdata.txt}. Lines may be in any order; like a KSDS, the records are returned in
 * ascending {@code ACCT-ID} order. Short lines are padded with spaces.
 */
public final class AccountFile implements Iterable<String> {

  private final List<String> records;

  private AccountFile(List<String> records) {
    this.records = List.copyOf(records);
  }

  /**
   * Loads an account unload.
   *
   * @param path the unload file
   * @return the file, sorted by key
   * @throws IOException if the file cannot be read
   * @throws InvalidAccountFileException if a record is too long or a key is duplicated
   */
  public static AccountFile open(Path path) throws IOException {
    Map<String, String> byKey = new TreeMap<>();
    int lineNumber = 0;
    for (String line : Files.readAllLines(path, StandardCharsets.ISO_8859_1)) {
      lineNumber++;
      if (line.length() > Account.RECORD_LENGTH) {
        throw new InvalidAccountFileException(
            "line " + lineNumber + " is " + line.length() + " bytes, the KSDS LRECL is 300");
      }
      String record = Alphanumeric.fit(line, Account.RECORD_LENGTH);
      String key = record.substring(0, Account.KEY_LENGTH);
      if (byKey.putIfAbsent(key, record) != null) {
        throw new InvalidAccountFileException("line " + lineNumber + " duplicates key " + key);
      }
    }
    return new AccountFile(new ArrayList<>(byKey.values()));
  }

  /** Returns the number of records. */
  public int size() {
    return records.size();
  }

  @Override
  public Iterator<String> iterator() {
    return records.iterator();
  }

  /** The unload cannot be a KSDS image: a record is longer than 300 bytes or a key repeats. */
  public static final class InvalidAccountFileException extends IOException {

    private static final long serialVersionUID = 1L;

    InvalidAccountFileException(String message) {
      super(message);
    }
  }
}
