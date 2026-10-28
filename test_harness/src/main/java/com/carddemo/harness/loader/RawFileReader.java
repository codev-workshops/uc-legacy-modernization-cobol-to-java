package com.carddemo.harness.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RawFileReader {
  public RawRecords readAscii(Path path, int recordLength) throws IOException {
    byte[] data = Files.readAllBytes(path);
    List<byte[]> records = new ArrayList<>();
    Map<String, Integer> terminators = new LinkedHashMap<>();
    int padded = 0;
    int start = 0;
    while (start < data.length) {
      int end = start;
      while (end < data.length && data[end] != '\n') {
        end++;
      }
      boolean hasLf = end < data.length;
      int length = end - start;
      String terminator = "NONE";
      if (length > 0 && data[start + length - 1] == '\r') {
        length--;
        terminator = "CRLF";
      } else if (hasLf) {
        terminator = "LF";
      }
      terminators.merge(terminator, 1, Integer::sum);
      if (length > recordLength) {
        throw new IOException(path.getFileName() + " record " + records.size()
            + " length " + length + " > LRECL " + recordLength);
      }
      byte[] record = new byte[recordLength];
      java.util.Arrays.fill(record, (byte) ' ');
      System.arraycopy(data, start, record, 0, length);
      if (length < recordLength) {
        padded++;
      }
      records.add(record);
      start = hasLf ? end + 1 : data.length;
    }
    return new RawRecords(records, terminators, padded, data);
  }

  public RawRecords readFixed(Path path, int recordLength) throws IOException {
    byte[] data = Files.readAllBytes(path);
    if (data.length % recordLength != 0) {
      throw new IOException(path.getFileName() + " has " + data.length
          + " bytes, not a multiple of " + recordLength);
    }
    List<byte[]> records = new ArrayList<>();
    for (int offset = 0; offset < data.length; offset += recordLength) {
      records.add(java.util.Arrays.copyOfRange(data, offset, offset + recordLength));
    }
    return new RawRecords(records, Map.of(), 0, data);
  }
}
