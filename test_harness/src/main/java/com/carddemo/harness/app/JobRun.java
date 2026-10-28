package com.carddemo.harness.app;

import java.nio.file.Path;
import java.util.List;

public record JobRun(int exitCode, List<Path> outputFiles, String standardOutput, String standardError) {
  public JobRun {
    outputFiles = List.copyOf(outputFiles);
  }
}
