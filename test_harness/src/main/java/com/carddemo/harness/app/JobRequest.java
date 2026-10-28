package com.carddemo.harness.app;

import java.nio.file.Path;
import java.util.List;

public record JobRequest(String caseId, List<Path> inputFiles, Path outputDirectory) {
  public JobRequest {
    inputFiles = List.copyOf(inputFiles);
  }
}
