package com.carddemo.harness.recon;

import java.util.List;
import java.util.Map;

public record ReconReport(
    Map<String, DatasetRecon> datasets,
    List<ReferentialCheck> referentialIntegrity) {
  public ReconReport {
    datasets = Map.copyOf(datasets);
    referentialIntegrity = List.copyOf(referentialIntegrity);
  }
}
