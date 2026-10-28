package com.carddemo.harness.app;

import java.nio.file.Path;
import java.util.Optional;

public interface MigratedAppAdapter {
  Optional<Path> outputFile(String caseId, String datasetName);

  Optional<JobRun> run(JobRequest request) throws Exception;
}
