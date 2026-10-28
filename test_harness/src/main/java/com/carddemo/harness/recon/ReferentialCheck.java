package com.carddemo.harness.recon;

import java.util.List;

public record ReferentialCheck(
    String check,
    int childRecords,
    int orphanRecords,
    List<String> distinctOrphanKeys) {
  public ReferentialCheck {
    distinctOrphanKeys = List.copyOf(distinctOrphanKeys);
  }
}
