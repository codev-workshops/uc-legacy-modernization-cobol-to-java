package com.carddemo.harness.report;

import com.carddemo.harness.comparator.CompareResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a detailed comparison report between COBOL and Java output files.
 */
public class ComparisonReport {

    private final String programName;
    private final String fileName;
    private int cobolRecordCount;
    private int javaRecordCount;
    private final List<RecordComparison> records = new ArrayList<>();

    public ComparisonReport(String programName, String fileName) {
        this.programName = programName;
        this.fileName = fileName;
    }

    public void setRecordCounts(int cobolCount, int javaCount) {
        this.cobolRecordCount = cobolCount;
        this.javaRecordCount = javaCount;
    }

    public void addRecord(RecordComparison record) {
        records.add(record);
    }

    public int getTotalMatches() {
        return records.stream().mapToInt(RecordComparison::getMatchCount).sum();
    }

    public int getTotalMismatches() {
        return records.stream().mapToInt(RecordComparison::getMismatchCount).sum();
    }

    public int getTotalSkipped() {
        return records.stream().mapToInt(RecordComparison::getSkippedCount).sum();
    }

    public int getTotalFieldsCompared() {
        return getTotalMatches() + getTotalMismatches() + getTotalSkipped();
    }

    public boolean isFullMatch() {
        return cobolRecordCount == javaRecordCount && getTotalMismatches() == 0;
    }

    public String generate() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.printf("=== %s Output Comparison Report ===%n", programName);
        pw.printf("File: %s%n", fileName);
        pw.printf("Record Count: COBOL=%d, Java=%d [%s]%n%n",
                cobolRecordCount, javaRecordCount,
                cobolRecordCount == javaRecordCount ? "MATCH" : "MISMATCH");

        for (RecordComparison rec : records) {
            pw.printf("Record %03d:%n", rec.getRecordNumber());
            for (FieldComparisonDetail detail : rec.getDetails()) {
                pw.println(detail);
            }
            pw.println();
        }

        pw.printf("Summary: %d records, %d fields, %d MATCH, %d MISMATCH, %d SKIPPED (filler)%n",
                records.size(), getTotalFieldsCompared(),
                getTotalMatches(), getTotalMismatches(), getTotalSkipped());

        pw.flush();
        return sw.toString();
    }

    public String getProgramName() {
        return programName;
    }

    public String getFileName() {
        return fileName;
    }

    public List<RecordComparison> getRecords() {
        return records;
    }

    /**
     * Comparison details for a single record.
     */
    public static class RecordComparison {
        private final int recordNumber;
        private final List<FieldComparisonDetail> details = new ArrayList<>();

        public RecordComparison(int recordNumber) {
            this.recordNumber = recordNumber;
        }

        public void addDetail(FieldComparisonDetail detail) {
            details.add(detail);
        }

        public int getRecordNumber() {
            return recordNumber;
        }

        public List<FieldComparisonDetail> getDetails() {
            return details;
        }

        public int getMatchCount() {
            return (int) details.stream().filter(d -> d.getResult() == CompareResult.MATCH).count();
        }

        public int getMismatchCount() {
            return (int) details.stream().filter(d -> d.getResult() == CompareResult.MISMATCH).count();
        }

        public int getSkippedCount() {
            return (int) details.stream().filter(d -> d.getResult() == CompareResult.SKIPPED).count();
        }
    }
}
