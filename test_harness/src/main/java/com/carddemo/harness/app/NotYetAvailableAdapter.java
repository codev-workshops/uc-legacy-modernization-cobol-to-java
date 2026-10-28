package com.carddemo.harness.app;

import java.nio.file.Path;
import java.util.Optional;

public final class NotYetAvailableAdapter implements MigratedAppAdapter {
  // MIGRATED-APP INTEGRATION POINT: replace this stub with an adapter for the migrated application.
  @Override
  public Optional<Path> outputFile(String caseId, String datasetName) {
    return Optional.empty();
  }

  // MIGRATED-APP INTEGRATION POINT: run the job using file inputs and return output file paths.
  @Override
  public Optional<JobRun> run(JobRequest request) {
    return Optional.empty();
  }
}
