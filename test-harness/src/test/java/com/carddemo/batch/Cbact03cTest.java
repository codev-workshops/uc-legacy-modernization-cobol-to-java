package com.carddemo.batch;

import com.carddemo.batch.model.CardXrefRecord;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Cbact03cTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String INPUT_DIR = "cbact03c-tests/data/input/";
    private static final String GOLDEN_DIR = "cbact03c-tests/data/golden/";

    // --- Golden data POJOs ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GoldenData {
        @JsonProperty("testCaseId") public String testCaseId;
        @JsonProperty("description") public String description;
        @JsonProperty("inputFile") public String inputFile;
        @JsonProperty("expectedOutput") public ExpectedOutput expectedOutput;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ExpectedOutput {
        @JsonProperty("headerLine") public String headerLine;
        @JsonProperty("footerLine") public String footerLine;
        @JsonProperty("records") public List<RecordExpectation> records;
        @JsonProperty("totalDisplayLines") public int totalDisplayLines;
        @JsonProperty("abend") public boolean abend;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RecordExpectation {
        @JsonProperty("recordIndex") public int recordIndex;
        @JsonProperty("rawLine") public String rawLine;
        @JsonProperty("fields") public Map<String, Object> fields;
        @JsonProperty("displayCount") public int displayCount;
    }

    // --- Helper methods ---

    private GoldenData loadGolden(String testCaseId) throws IOException {
        File goldenFile = Path.of(GOLDEN_DIR, testCaseId + "_golden.json").toFile();
        return MAPPER.readValue(goldenFile, GoldenData.class);
    }

    private String runProgram(String inputFilePath) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Cbact03c.run(inputFilePath, pw);
        pw.flush();
        return sw.toString();
    }

    private String[] runAndGetLines(String inputFilePath) {
        String output = runProgram(inputFilePath);
        if (output.isEmpty()) {
            return new String[0];
        }
        return output.split("\\r?\\n");
    }

    private void verifyGoldenOutput(String testCaseId, String inputFile) throws IOException {
        GoldenData golden = loadGolden(testCaseId);
        ExpectedOutput expected = golden.expectedOutput;
        String inputPath = INPUT_DIR + inputFile;

        String[] lines = runAndGetLines(inputPath);

        // Verify header
        assertTrue(lines.length > 0, "Output should have at least a header line");
        assertEquals(expected.headerLine, lines[0], "Header line mismatch");

        // Verify footer
        assertEquals(expected.footerLine, lines[lines.length - 1], "Footer line mismatch");

        // Verify total display lines
        assertEquals(expected.totalDisplayLines, lines.length,
                "Total line count mismatch (expected " + expected.totalDisplayLines + ")");

        // Verify each record
        for (RecordExpectation rec : expected.records) {
            int expectedCount = rec.displayCount;
            long actualCount = Arrays.stream(lines)
                    .filter(l -> l.equals(rec.rawLine))
                    .count();
            assertEquals(expectedCount, actualCount,
                    "Record at index " + rec.recordIndex
                            + " should appear " + expectedCount + " times: " + rec.rawLine);

            // Verify field parsing from rawLine
            CardXrefRecord parsed = new CardXrefRecord(rec.rawLine);
            assertEquals(rec.fields.get("XREF-CARD-NUM"), parsed.getXrefCardNum(),
                    "XREF-CARD-NUM mismatch for record " + rec.recordIndex);

            Object custIdObj = rec.fields.get("XREF-CUST-ID");
            long expectedCustId = ((Number) custIdObj).longValue();
            assertEquals(expectedCustId, Long.parseLong(parsed.getXrefCustId()),
                    "XREF-CUST-ID mismatch for record " + rec.recordIndex);

            Object acctIdObj = rec.fields.get("XREF-ACCT-ID");
            long expectedAcctId = ((Number) acctIdObj).longValue();
            assertEquals(expectedAcctId, Long.parseLong(parsed.getXrefAcctId()),
                    "XREF-ACCT-ID mismatch for record " + rec.recordIndex);

            assertEquals(rec.fields.get("FILLER"), parsed.getFiller(),
                    "FILLER mismatch for record " + rec.recordIndex);
        }

        assertFalse(expected.abend, "Test case should not abend");
    }

    // --- Happy path tests ---

    @Test
    void testSingleRecord() throws IOException {
        verifyGoldenOutput("tc01", "tc01_single_record.dat");
    }

    @Test
    void testMultipleRecords() throws IOException {
        verifyGoldenOutput("tc02", "tc02_multiple_records.dat");
    }

    @Test
    void testLargeDataset() throws IOException {
        verifyGoldenOutput("tc03", "tc03_large_dataset.dat");
    }

    @Test
    void testZeroIds() throws IOException {
        verifyGoldenOutput("tc04", "tc04_zero_ids.dat");
    }

    // --- Boundary tests ---

    @Test
    void testLeadingSpacesCardNum() throws IOException {
        verifyGoldenOutput("tc05", "tc05_leading_spaces.dat");
    }

    @Test
    void testSpecialCharsCardNum() throws IOException {
        verifyGoldenOutput("tc06", "tc06_special_chars.dat");
    }

    @Test
    void testMaxCustId() throws IOException {
        verifyGoldenOutput("tc07", "tc07_max_cust_id.dat");
    }

    @Test
    void testMaxAcctId() throws IOException {
        verifyGoldenOutput("tc08", "tc08_max_acct_id.dat");
    }

    @Test
    void testAllSpacesCardNum() throws IOException {
        verifyGoldenOutput("tc09", "tc09_all_spaces_cardnum.dat");
    }

    @Test
    void testNonEmptyFiller() throws IOException {
        verifyGoldenOutput("tc10", "tc10_nonempty_filler.dat");
    }

    @Test
    void testTypicalCards() throws IOException {
        verifyGoldenOutput("tc12", "tc12_typical_cards.dat");
    }

    // --- Edge case tests ---

    @Test
    void testInitializedFiller() throws IOException {
        verifyGoldenOutput("tc13", "tc13_initialized_filler.dat");
    }

    @Test
    void testNonUniqueAix() throws IOException {
        verifyGoldenOutput("tc18", "tc18_nonunique_aix.dat");
    }

    // --- Error/boundary tests ---

    @Test
    void testEmptyFile() throws IOException {
        GoldenData golden = loadGolden("tc19");
        ExpectedOutput expected = golden.expectedOutput;
        String inputPath = INPUT_DIR + "tc19_empty.dat";

        String[] lines = runAndGetLines(inputPath);

        assertEquals(expected.totalDisplayLines, lines.length,
                "Empty file should produce only header + footer");
        assertEquals(expected.headerLine, lines[0]);
        assertEquals(expected.footerLine, lines[1]);
        assertTrue(expected.records.isEmpty());
    }

    @Test
    void testFileNotFound() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> Cbact03c.run("nonexistent/path/to/file.dat", pw));

        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("ERROR OPENING XREFFILE"),
                "Should print error opening message");
        assertTrue(output.contains("ABENDING PROGRAM"),
                "Should print abending message");
        assertEquals("ABEND with code 999", ex.getMessage());
    }

    @Test
    void testDuplicateDisplayBehavior() throws IOException {
        // Use tc02 (5 records) to verify each record appears exactly twice
        GoldenData golden = loadGolden("tc02");
        String inputPath = INPUT_DIR + "tc02_multiple_records.dat";
        String[] lines = runAndGetLines(inputPath);

        // Skip header (index 0) and footer (last index)
        // Record lines should be pairs
        int recordLineCount = lines.length - 2; // minus header and footer
        assertEquals(golden.expectedOutput.records.size() * 2, recordLineCount,
                "Each record should produce exactly 2 display lines");

        for (int i = 0; i < golden.expectedOutput.records.size(); i++) {
            String expectedRaw = golden.expectedOutput.records.get(i).rawLine;
            // Lines at 1-based offset: (i*2)+1 and (i*2)+2
            assertEquals(expectedRaw, lines[(i * 2) + 1],
                    "First display of record " + i);
            assertEquals(expectedRaw, lines[(i * 2) + 2],
                    "Second display of record " + i);
        }
    }

    // --- Output format tests ---

    @Test
    void testOutputLineLength() throws IOException {
        String inputPath = INPUT_DIR + "tc02_multiple_records.dat";
        String[] lines = runAndGetLines(inputPath);

        // Record lines (skip header at 0, footer at last)
        for (int i = 1; i < lines.length - 1; i++) {
            assertEquals(CardXrefRecord.RECORD_LENGTH, lines[i].length(),
                    "Record display line " + i + " should be exactly 50 characters");
        }
    }

    @Test
    void testTotalLineCount() throws IOException {
        // For tc02: 5 records => 1 header + 2*5 records + 1 footer = 12
        GoldenData golden = loadGolden("tc02");
        String inputPath = INPUT_DIR + "tc02_multiple_records.dat";
        String[] lines = runAndGetLines(inputPath);

        int expectedRecords = golden.expectedOutput.records.size();
        int expectedLines = 1 + (2 * expectedRecords) + 1;
        assertEquals(expectedLines, lines.length,
                "Total lines = 1(header) + 2*N(records) + 1(footer)");
    }
}
