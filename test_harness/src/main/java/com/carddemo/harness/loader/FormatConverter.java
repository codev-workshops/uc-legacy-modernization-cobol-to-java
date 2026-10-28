package com.carddemo.harness.loader;

import com.carddemo.harness.codec.CardDemoLayouts;
import com.carddemo.harness.codec.RecordCodec;
import com.carddemo.harness.codec.RecordLayout;
import com.carddemo.harness.codec.SourceEncoding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FormatConverter {
  private final RawFileReader reader;
  private final GoldenDataLoader golden;

  public FormatConverter() {
    this(new RawFileReader(), new GoldenDataLoader());
  }

  public FormatConverter(RawFileReader reader, GoldenDataLoader golden) {
    this.reader = reader;
    this.golden = golden;
  }

  public List<Map<String, Object>> rawToGolden(String dataset, SourceEncoding encoding)
      throws IOException {
    RecordLayout layout = CardDemoLayouts.get(dataset);
    Map<String, Object> metadata = golden.datasetManifest(dataset);
    String source;
    if (encoding == SourceEncoding.EBCDIC_CP037 && !dataset.equalsIgnoreCase("export")) {
      Map<String, Object> counterpart =
          GoldenDataLoader.object(metadata.get("ebcdicCounterpart"));
      source = String.valueOf(counterpart.get("file"));
    } else {
      source = String.valueOf(metadata.get("sourceFile"));
    }
    RawRecords raw = encoding == SourceEncoding.ASCII
        ? reader.readAscii(new RepoPaths().resolve(source), layout.recordLength())
        : reader.readFixed(new RepoPaths().resolve(source), layout.recordLength());
    RecordCodec codec = new RecordCodec(layout);
    return raw.records().stream()
        .<Map<String, Object>>map(record -> codec.decode(record, encoding)).toList();
  }

  public List<Map<String, Object>> rawToGolden(
      String dataset, byte[] bytes, SourceEncoding encoding) throws IOException {
    RecordLayout layout = CardDemoLayouts.get(dataset);
    java.nio.file.Path scratch = java.nio.file.Files.createTempFile("carddemo-records", ".dat");
    try {
      java.nio.file.Files.write(scratch, bytes);
      RawRecords records = encoding == SourceEncoding.ASCII
          ? reader.readAscii(scratch, layout.recordLength())
          : reader.readFixed(scratch, layout.recordLength());
      RecordCodec codec = new RecordCodec(layout);
      return records.records().stream()
          .<Map<String, Object>>map(record -> codec.decode(record, encoding)).toList();
    } finally {
      java.nio.file.Files.deleteIfExists(scratch);
    }
  }

  public byte[] goldenToRaw(String dataset, List<Map<String, Object>> records,
      SourceEncoding encoding) {
    RecordCodec codec = new RecordCodec(CardDemoLayouts.get(dataset));
    List<byte[]> output = new ArrayList<>();
    for (Map<String, Object> record : records) {
      output.add(codec.encode(record, encoding));
    }
    int size = output.stream().mapToInt(record -> record.length).sum();
    byte[] combined = new byte[size];
    int offset = 0;
    for (byte[] record : output) {
      System.arraycopy(record, 0, combined, offset, record.length);
      offset += record.length;
    }
    return combined;
  }

  public byte[] goldenToAsciiLines(String dataset, List<Map<String, Object>> records) {
    byte[] fixed = goldenToRaw(dataset, records, SourceEncoding.ASCII);
    int recordLength = CardDemoLayouts.get(dataset).recordLength();
    byte[] lines = new byte[fixed.length + records.size()];
    for (int index = 0; index < records.size(); index++) {
      System.arraycopy(fixed, index * recordLength, lines, index * (recordLength + 1), recordLength);
      lines[index * (recordLength + 1) + recordLength] = '\n';
    }
    return lines;
  }
}
