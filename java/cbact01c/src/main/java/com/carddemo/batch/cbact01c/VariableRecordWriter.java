package com.carddemo.batch.cbact01c;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes {@code VBRCFILE}: {@code RECORD IS VARYING IN SIZE FROM 10 TO 80}, each record preceded by
 * a 4-byte length prefix in the chosen {@link RdwFormat}.
 */
public final class VariableRecordWriter implements Closeable {

  /** Smallest record length the FD allows. */
  public static final int MIN_LENGTH = 10;

  /** Largest record length the FD allows ({@code VBR-REC PIC X(80)}). */
  public static final int MAX_LENGTH = 80;

  private final OutputStream out;
  private final RdwFormat format;

  /**
   * Creates (or replaces) the dataset.
   *
   * @param path the output file
   * @param format the record prefix format
   * @throws IOException if the file cannot be created
   */
  public VariableRecordWriter(Path path, RdwFormat format) throws IOException {
    this.out = new BufferedOutputStream(Files.newOutputStream(path));
    this.format = format;
  }

  /**
   * Writes one record.
   *
   * @param record the record
   * @throws IOException if the write fails
   */
  public void write(VariableRecord record) throws IOException {
    byte[] data = record.toBytes();
    if (data.length < MIN_LENGTH || data.length > MAX_LENGTH) {
      throw new IllegalArgumentException("record length " + data.length + " is outside 10-80");
    }
    out.write(format.prefix(data.length));
    out.write(data);
  }

  @Override
  public void close() throws IOException {
    out.close();
  }
}
