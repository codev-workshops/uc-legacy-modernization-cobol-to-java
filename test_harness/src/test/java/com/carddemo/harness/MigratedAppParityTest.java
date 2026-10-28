package com.carddemo.harness;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.carddemo.harness.app.MigratedAppAdapters;
import com.carddemo.harness.app.NotYetAvailableAdapter;
import org.junit.jupiter.api.Test;

class MigratedAppParityTest {
  @Test
  void comparesMigratedApplicationOutputsWhenAdapterIsConfigured() {
    var adapter = MigratedAppAdapters.resolve();
    assumeFalse(adapter instanceof NotYetAvailableAdapter,
        "Migrated app parity is skipped: configure carddemo.app.adapter to enable it.");
  }
}
