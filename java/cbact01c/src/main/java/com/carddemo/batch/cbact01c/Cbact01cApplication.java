package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.AccountExtractJob.Datasets;
import com.carddemo.batch.cbact01c.JobOptions.CycleDebitPolicy;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Command-line entry point, the equivalent of {@code EXEC PGM=CBACT01C}.
 *
 * <pre>
 * java -jar cbact01c.jar ACCTFILE OUTFILE ARRYFILE VBRCFILE
 *     [--rdw=zos|gnucobol] [--cycle-debit=legacy|copy-source]
 * </pre>
 *
 * <p>Exit status: 0 on success, 16 on an abend, 2 on a usage error.
 */
public final class Cbact01cApplication {

  /** Exit status when the job abends. */
  public static final int ABEND_EXIT = 16;

  /** Exit status for bad arguments. */
  public static final int USAGE_EXIT = 2;

  private static final String USAGE =
      "usage: cbact01c ACCTFILE OUTFILE ARRYFILE VBRCFILE"
          + " [--rdw=zos|gnucobol] [--cycle-debit=legacy|copy-source]";

  private Cbact01cApplication() {}

  /**
   * Runs the job.
   *
   * @param args the DD paths and options
   */
  public static void main(String[] args) {
    System.exit(run(args, new PrintStream(System.out, true, StandardCharsets.ISO_8859_1)));
  }

  /**
   * Runs the job and returns the exit status instead of exiting.
   *
   * @param args the DD paths and options
   * @param sysout where {@code DISPLAY} output goes
   * @return the exit status
   */
  static int run(String[] args, PrintStream sysout) {
    List<String> paths = new ArrayList<>();
    RdwFormat rdw = JobOptions.DEFAULTS.rdwFormat();
    CycleDebitPolicy policy = JobOptions.DEFAULTS.cycleDebitPolicy();
    try {
      for (String arg : args) {
        if (arg.startsWith("--rdw=")) {
          rdw = RdwFormat.valueOf(optionValue(arg));
        } else if (arg.startsWith("--cycle-debit=")) {
          policy = CycleDebitPolicy.valueOf(optionValue(arg));
        } else if (arg.startsWith("--")) {
          throw new IllegalArgumentException("unknown option " + arg);
        } else {
          paths.add(arg);
        }
      }
      if (paths.size() != 4) {
        throw new IllegalArgumentException("expected 4 dataset paths, got " + paths.size());
      }
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.err.println(USAGE);
      return USAGE_EXIT;
    }
    Datasets datasets =
        new Datasets(
            Path.of(paths.get(0)),
            Path.of(paths.get(1)),
            Path.of(paths.get(2)),
            Path.of(paths.get(3)));
    try {
      new AccountExtractJob(new JobOptions(rdw, policy), sysout).run(datasets);
      return 0;
    } catch (AbendException e) {
      System.err.println("CBACT01C ABEND " + e.getMessage());
      return ABEND_EXIT;
    }
  }

  private static String optionValue(String arg) {
    return arg.substring(arg.indexOf('=') + 1).replace('-', '_').toUpperCase(Locale.ROOT);
  }
}
