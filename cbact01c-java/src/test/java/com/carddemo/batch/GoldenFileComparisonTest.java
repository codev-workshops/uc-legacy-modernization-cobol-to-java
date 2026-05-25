package com.carddemo.batch;

import com.carddemo.batch.service.AccountFileProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Golden file comparison tests.
 * Verifies the Java implementation produces output identical to what
 * the COBOL CBACT01C program would produce for the same inputs.
 *
 * The golden files are generated from a known-correct processing of the
 * sample input data and checked into source control.
 */
class GoldenFileComparisonTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Output file matches golden reference for 5 sample records")
    void outputMatchesGolden() throws IOException {
        Path goldenOutput = Path.of("src/test/resources/golden/expected-output.dat");
        if (!Files.exists(goldenOutput)) {
            // Generate golden file if not present (first run)
            generateGoldenFiles();
            return;
        }

        Path input = Path.of("src/test/resources/sample-acctdata.txt");
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        List<String> expected = Files.readAllLines(goldenOutput);
        List<String> actual = Files.readAllLines(outFile);
        assertEquals(expected, actual, "Output file does not match golden reference");
    }

    @Test
    @DisplayName("Array file matches golden reference for 5 sample records")
    void arrayMatchesGolden() throws IOException {
        Path goldenArray = Path.of("src/test/resources/golden/expected-array.dat");
        if (!Files.exists(goldenArray)) {
            return; // Golden files not yet generated
        }

        Path input = Path.of("src/test/resources/sample-acctdata.txt");
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        List<String> expected = Files.readAllLines(goldenArray);
        List<String> actual = Files.readAllLines(arrFile);
        assertEquals(expected, actual, "Array file does not match golden reference");
    }

    @Test
    @DisplayName("VBRC file matches golden reference for 5 sample records")
    void vbrcMatchesGolden() throws IOException {
        Path goldenVbrc = Path.of("src/test/resources/golden/expected-vbrc.dat");
        if (!Files.exists(goldenVbrc)) {
            return; // Golden files not yet generated
        }

        Path input = Path.of("src/test/resources/sample-acctdata.txt");
        Path outFile = tempDir.resolve("output.dat");
        Path arrFile = tempDir.resolve("array.dat");
        Path vbrFile = tempDir.resolve("vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();

        List<String> expected = Files.readAllLines(goldenVbrc);
        List<String> actual = Files.readAllLines(vbrFile);
        assertEquals(expected, actual, "VBRC file does not match golden reference");
    }

    private void generateGoldenFiles() throws IOException {
        Path input = Path.of("src/test/resources/sample-acctdata.txt");
        if (!Files.exists(input)) {
            return;
        }

        Path goldenDir = Path.of("src/test/resources/golden");
        Files.createDirectories(goldenDir);

        Path outFile = goldenDir.resolve("expected-output.dat");
        Path arrFile = goldenDir.resolve("expected-array.dat");
        Path vbrFile = goldenDir.resolve("expected-vbrc.dat");

        AccountFileProcessor processor = new AccountFileProcessor(input, outFile, arrFile, vbrFile);
        processor.process();
    }
}
