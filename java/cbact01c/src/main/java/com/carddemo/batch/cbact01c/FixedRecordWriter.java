package com.carddemo.batch.cbact01c;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes a {@code RECFM=FB} dataset: fixed-length records with no separators. */
public final class FixedRecordWriter implements Closeable {

  private final OutputStream out;
  private final int recordLength;

  /**
   * Creates (or replaces) the dataset.
   *
   * @param path the output file
   * @param recordLength the {@code LRECL}
   * @throws IOException if the file cannot be created
   */
  public FixedRecordWriter(Path path, int recordLength) throws IOException {
    this.out = new BufferedOutputStream(Files.newOutputStream(path));
    this.recordLength = recordLength;
  }

  /**
   * Writes one record.
   *
   * @param record exactly {@code LRECL} bytes
   * @throws IOException if the write fails
   */
  public void write(byte[] record) throws IOException {
    if (record.length != recordLength) {
      throw new IllegalArgumentException(
          "record is " + record.length + " bytes, LRECL is " + recordLength);
    }
    out.write(record);
  }

  @Override
  public void close() throws IOException {
    out.close();
  }
}
