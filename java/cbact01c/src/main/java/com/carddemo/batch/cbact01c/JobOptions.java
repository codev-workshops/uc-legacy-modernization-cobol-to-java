package com.carddemo.batch.cbact01c;

import java.util.Objects;

/**
 * Run options.
 *
 * @param rdwFormat the record prefix written to {@code VBRCFILE}
 * @param cycleDebitPolicy how {@code OUT-ACCT-CURR-CYC-DEBIT} is filled
 */
public record JobOptions(RdwFormat rdwFormat, CycleDebitPolicy cycleDebitPolicy) {

  /** z/OS RDWs and the original program's cycle-debit behaviour. */
  public static final JobOptions DEFAULTS = new JobOptions(RdwFormat.ZOS, CycleDebitPolicy.LEGACY);

  /** Rejects null options. */
  public JobOptions {
    Objects.requireNonNull(rdwFormat, "rdwFormat");
    Objects.requireNonNull(cycleDebitPolicy, "cycleDebitPolicy");
  }

  /** How {@code OUT-ACCT-CURR-CYC-DEBIT} is filled for accounts whose cycle debit is not zero. */
  public enum CycleDebitPolicy {

    /**
     * As CBACT01C: only a zero debit is replaced (by 2525.00); otherwise the field keeps its
     * previous contents, i.e. the value written for the previous account, or low-values.
     */
    LEGACY,

    /** Corrected: a zero debit becomes 2525.00, any other debit is written as it is. */
    COPY_SOURCE
  }
}
