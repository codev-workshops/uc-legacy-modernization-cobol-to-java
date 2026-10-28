package com.carddemo.harness;

import com.carddemo.harness.codec.CardDemoLayouts;
import com.carddemo.harness.codec.SourceEncoding;
import com.carddemo.harness.loader.GoldenDataLoader;
import com.carddemo.harness.loader.RawFileReader;
import com.carddemo.harness.loader.RawRecords;
import com.carddemo.harness.loader.RepoPaths;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

final class HarnessTestData {
  static final List<String> DATASETS = List.of("acctdata", "carddata", "cardxref", "custdata",
      "dailytran", "discgrp", "tcatbal", "trancatg", "trantype", "export");

  private static final RepoPaths PATHS = new RepoPaths();
  private static final GoldenDataLoader GOLDEN = new GoldenDataLoader(PATHS);
  private static final RawFileReader READER = new RawFileReader();

  private HarnessTestData() {}

  static List<Map<String, Object>> golden(String dataset) throws IOException {
    return GOLDEN.records(dataset);
  }

  static Map<String, Object> manifest() throws IOException {
    return GOLDEN.manifest();
  }

  static Map<String, Object> datasetManifest(String dataset) throws IOException {
    return GOLDEN.datasetManifest(dataset);
  }

  static RawRecords ascii(String dataset) throws IOException {
    return READER.readAscii(PATHS.resolve(String.valueOf(datasetManifest(dataset).get("sourceFile"))),
        CardDemoLayouts.get(dataset).recordLength());
  }

  static RawRecords ebcdicCounterpart(String dataset) throws IOException {
    Map<String, Object> counterpart = GoldenDataLoader.object(
        datasetManifest(dataset).get("ebcdicCounterpart"));
    return READER.readFixed(PATHS.resolve(String.valueOf(counterpart.get("file"))),
        CardDemoLayouts.get(dataset).recordLength());
  }

  static RawRecords source(String dataset) throws IOException {
    if (dataset.equals("export")) {
      return READER.readFixed(PATHS.resolve(String.valueOf(datasetManifest(dataset).get("sourceFile"))),
          CardDemoLayouts.get(dataset).recordLength());
    }
    return ascii(dataset);
  }

  static SourceEncoding sourceEncoding(String dataset) {
    return dataset.equals("export") ? SourceEncoding.EBCDIC_CP037 : SourceEncoding.ASCII;
  }

  static Path repoRoot() {
    return PATHS.root();
  }
}
