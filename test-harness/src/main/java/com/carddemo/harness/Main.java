package com.carddemo.harness;

import com.carddemo.harness.comparator.RecordComparator;
import com.carddemo.harness.config.ToleranceConfig;
import com.carddemo.harness.parser.RecordLayout;
import com.carddemo.harness.report.ComparisonReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Entry point for the COBOL-to-Java test harness.
 *
 * Usage:
 *   mvn exec:java -Dexec.mainClass=com.carddemo.harness.Main \
 *       -Dexec.args="--cobol-dir=/path/to/cobol/output --java-dir=/path/to/java/output"
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        String cobolDir = null;
        String javaDir = null;
        String toleranceFile = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--cobol-dir=")) {
                cobolDir = args[i].substring("--cobol-dir=".length());
            } else if (args[i].startsWith("--java-dir=")) {
                javaDir = args[i].substring("--java-dir=".length());
            } else if (args[i].startsWith("--tolerance=")) {
                toleranceFile = args[i].substring("--tolerance=".length());
            }
        }

        if (cobolDir == null || javaDir == null) {
            System.err.println("Usage: Main --cobol-dir=<path> --java-dir=<path> [--tolerance=<file>]");
            System.exit(1);
        }

        ToleranceConfig config = loadToleranceConfig(toleranceFile);
        RecordComparator comparator = new RecordComparator();
        boolean allPassed = true;

        // Compare OUTFILE
        allPassed &= compareIfExists(comparator, cobolDir, javaDir, "OUTFILE",
                RecordLayout.outfileLayout(), "CBACT01C", config);

        // Compare ARRYFILE
        allPassed &= compareIfExists(comparator, cobolDir, javaDir, "ARRYFILE",
                RecordLayout.arryFileLayout(), "CBACT01C", config);

        // Compare ACCTDATA
        allPassed &= compareIfExists(comparator, cobolDir, javaDir, "ACCTDATA",
                RecordLayout.accountRecordLayout(), "ACCOUNT", config);

        // Compare TRANFILE
        allPassed &= compareIfExists(comparator, cobolDir, javaDir, "TRANFILE",
                RecordLayout.tranRecordLayout(), "TRANSACTION", config);

        // Compare VBRCFILE (variable-length)
        Path cobolVbrc = Paths.get(cobolDir, "VBRCFILE");
        Path javaVbrc = Paths.get(javaDir, "VBRCFILE");
        if (Files.exists(cobolVbrc) && Files.exists(javaVbrc)) {
            LOG.info("Comparing VBRCFILE (variable-length)...");
            ComparisonReport vbrcReport = comparator.compareVariableLength(
                    cobolVbrc, javaVbrc,
                    RecordLayout.vbrcFileRec1Layout(),
                    RecordLayout.vbrcFileRec2Layout(),
                    "CBACT01C", config, config.isStripRdw());
            System.out.println(vbrcReport.generate());
            if (!vbrcReport.isFullMatch()) {
                allPassed = false;
            }
        }

        System.out.println();
        System.out.println(allPassed ? "OVERALL RESULT: ALL COMPARISONS PASSED"
                : "OVERALL RESULT: SOME COMPARISONS FAILED");
        System.exit(allPassed ? 0 : 1);
    }

    private static boolean compareIfExists(RecordComparator comparator,
                                            String cobolDir, String javaDir, String fileName,
                                            RecordLayout layout, String programName,
                                            ToleranceConfig config) throws IOException {
        Path cobolFile = Paths.get(cobolDir, fileName);
        Path javaFile = Paths.get(javaDir, fileName);

        if (!Files.exists(cobolFile) || !Files.exists(javaFile)) {
            LOG.warn("Skipping {} — file not found in one or both directories", fileName);
            return true;
        }

        LOG.info("Comparing {}...", fileName);
        ComparisonReport report = comparator.compareFixedLength(
                cobolFile, javaFile, layout, programName, config);
        System.out.println(report.generate());
        return report.isFullMatch();
    }

    private static ToleranceConfig loadToleranceConfig(String filePath) {
        ToleranceConfig config = new ToleranceConfig();

        if (filePath == null) {
            return config;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            props.load(fis);
        } catch (IOException e) {
            LOG.warn("Could not load tolerance file: {}. Using defaults.", filePath);
            return config;
        }

        if (props.containsKey("numeric.tolerance")) {
            config.setNumericTolerance(new BigDecimal(props.getProperty("numeric.tolerance")));
        }
        if (props.containsKey("rtrim.alphanumeric")) {
            config.setRtrimAlphanumeric(Boolean.parseBoolean(props.getProperty("rtrim.alphanumeric")));
        }
        if (props.containsKey("ignore.filler")) {
            config.setIgnoreFiller(Boolean.parseBoolean(props.getProperty("ignore.filler")));
        }
        if (props.containsKey("strip.rdw")) {
            config.setStripRdw(Boolean.parseBoolean(props.getProperty("strip.rdw")));
        }
        if (props.containsKey("normalize.dates")) {
            config.setNormalizeDates(Boolean.parseBoolean(props.getProperty("normalize.dates")));
        }

        return config;
    }
}
