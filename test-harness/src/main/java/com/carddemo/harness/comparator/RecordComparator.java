package com.carddemo.harness.comparator;

import com.carddemo.harness.codec.PackedDecimalCodec;
import com.carddemo.harness.codec.ZonedDecimalCodec;
import com.carddemo.harness.config.ToleranceConfig;
import com.carddemo.harness.parser.FieldDefinition;
import com.carddemo.harness.parser.RecordLayout;
import com.carddemo.harness.report.ComparisonReport;
import com.carddemo.harness.report.ComparisonReport.RecordComparison;
import com.carddemo.harness.report.FieldComparisonDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Orchestrates full record-by-record comparison of COBOL and Java output files.
 */
public class RecordComparator {

    private static final Logger LOG = LoggerFactory.getLogger(RecordComparator.class);

    private final FieldComparator fieldComparator = new FieldComparator();

    /**
     * Compare fixed-length record files.
     */
    public ComparisonReport compareFixedLength(Path cobolFile, Path javaFile,
                                                RecordLayout layout, String programName,
                                                ToleranceConfig config) throws IOException {
        byte[] cobolData = readAllBytes(cobolFile);
        byte[] javaData = readAllBytes(javaFile);

        int recLen = layout.getRecordLength();
        int cobolCount = cobolData.length / recLen;
        int javaCount = javaData.length / recLen;

        ComparisonReport report = new ComparisonReport(programName, layout.getName());
        report.setRecordCounts(cobolCount, javaCount);

        int compareCount = Math.min(cobolCount, javaCount);

        for (int r = 0; r < compareCount; r++) {
            RecordComparison recComp = new RecordComparison(r + 1);
            int recOffset = r * recLen;

            for (FieldDefinition field : layout.getFields()) {
                byte[] cobolField = Arrays.copyOfRange(cobolData,
                        recOffset + field.getOffset(),
                        recOffset + field.getOffset() + field.getLength());
                byte[] javaField = Arrays.copyOfRange(javaData,
                        recOffset + field.getOffset(),
                        recOffset + field.getOffset() + field.getLength());

                CompareResult result = fieldComparator.compare(cobolField, javaField, field, config);

                String cobolDisplay = formatFieldValue(cobolField, field, config);
                String javaDisplay = formatFieldValue(javaField, field, config);

                String annotation = buildAnnotation(field, config);
                recComp.addDetail(new FieldComparisonDetail(
                        field.getName(), cobolDisplay, javaDisplay, result, annotation));
            }

            report.addRecord(recComp);
        }

        return report;
    }

    /**
     * Compare variable-length record files (e.g., VBRC-FILE).
     * Records are differentiated by length: 12 bytes → REC1, 39 bytes → REC2.
     */
    public ComparisonReport compareVariableLength(Path cobolFile, Path javaFile,
                                                   RecordLayout rec1Layout, RecordLayout rec2Layout,
                                                   String programName, ToleranceConfig config,
                                                   boolean hasRdw) throws IOException {
        byte[] cobolData = readAllBytes(cobolFile);
        byte[] javaData = readAllBytes(javaFile);

        ComparisonReport report = new ComparisonReport(programName, "VBRC-FILE");

        int cobolRecords = countVbRecords(cobolData, rec1Layout, rec2Layout, hasRdw, config);
        int javaRecords = countVbRecords(javaData, rec1Layout, rec2Layout, hasRdw, config);
        report.setRecordCounts(cobolRecords, javaRecords);

        int cobolOffset = 0;
        int javaOffset = 0;
        int recordNum = 0;

        while (cobolOffset < cobolData.length && javaOffset < javaData.length) {
            recordNum++;

            int cobolRecLen = getVbRecordLength(cobolData, cobolOffset, hasRdw, config);
            int javaRecLen = getVbRecordLength(javaData, javaOffset, hasRdw, config);

            int cobolDataStart = hasRdw ? cobolOffset + 4 : cobolOffset;
            int javaDataStart = hasRdw ? javaOffset + 4 : javaOffset;

            RecordLayout layout = (cobolRecLen == rec1Layout.getRecordLength())
                    ? rec1Layout : rec2Layout;

            RecordComparison recComp = new RecordComparison(recordNum);

            if (cobolRecLen != javaRecLen) {
                recComp.addDetail(new FieldComparisonDetail(
                        "RECORD-LENGTH",
                        String.valueOf(cobolRecLen),
                        String.valueOf(javaRecLen),
                        CompareResult.MISMATCH,
                        "Variable record length mismatch"));
            } else {
                for (FieldDefinition field : layout.getFields()) {
                    if (field.getOffset() + field.getLength() > cobolRecLen) {
                        break;
                    }
                    byte[] cobolField = Arrays.copyOfRange(cobolData,
                            cobolDataStart + field.getOffset(),
                            cobolDataStart + field.getOffset() + field.getLength());
                    byte[] javaField = Arrays.copyOfRange(javaData,
                            javaDataStart + field.getOffset(),
                            javaDataStart + field.getOffset() + field.getLength());

                    CompareResult result = fieldComparator.compare(cobolField, javaField, field, config);
                    String cobolDisplay = formatFieldValue(cobolField, field, config);
                    String javaDisplay = formatFieldValue(javaField, field, config);
                    recComp.addDetail(new FieldComparisonDetail(
                            field.getName(), cobolDisplay, javaDisplay, result));
                }
            }

            report.addRecord(recComp);
            cobolOffset += (hasRdw ? 4 : 0) + cobolRecLen;
            javaOffset += (hasRdw ? 4 : 0) + javaRecLen;
        }

        return report;
    }

    private int getVbRecordLength(byte[] data, int offset, boolean hasRdw, ToleranceConfig config) {
        if (hasRdw && config.isStripRdw()) {
            // RDW: first 2 bytes = record length (big-endian), next 2 bytes = zeros
            return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
        }
        // Without RDW, need to determine from content — use heuristic based on known lengths
        // For VBRC-FILE, WS-RECD-LEN is either 12 or 39
        return guessVbRecordLength(data, offset);
    }

    private int guessVbRecordLength(byte[] data, int offset) {
        // Heuristic: if remaining data >= 39 and position 11 contains a digit, likely REC2
        int remaining = data.length - offset;
        if (remaining >= 39) {
            // Check if byte at offset+11 looks like part of a signed numeric (REC2 has curr-bal there)
            byte b = data[offset + 11];
            if ((b >= '0' && b <= '9') || b == '{' || b == '}' ||
                    (b >= 'A' && b <= 'I') || (b >= 'J' && b <= 'R')) {
                // Could be either — check if offset+12 also looks numeric
                if (remaining >= 39 && offset + 38 < data.length) {
                    return 39;
                }
            }
            return 12;
        }
        return remaining;
    }

    private int countVbRecords(byte[] data, RecordLayout rec1, RecordLayout rec2,
                                boolean hasRdw, ToleranceConfig config) {
        int count = 0;
        int offset = 0;
        while (offset < data.length) {
            int recLen = getVbRecordLength(data, offset, hasRdw, config);
            offset += (hasRdw ? 4 : 0) + recLen;
            count++;
        }
        return count;
    }

    String formatFieldValue(byte[] fieldData, FieldDefinition def, ToleranceConfig config) {
        return switch (def.getType()) {
            case ALPHANUMERIC -> new String(fieldData, config.getCobolEncoding());
            case DISPLAY_NUMERIC -> new String(fieldData, config.getCobolEncoding());
            case DISPLAY_SIGNED -> ZonedDecimalCodec.decode(fieldData, def.getScale()).toPlainString();
            case COMP3 -> PackedDecimalCodec.decode(fieldData, def.getScale()).toPlainString();
            case FILLER -> "<FILLER:" + fieldData.length + "bytes>";
        };
    }

    private String buildAnnotation(FieldDefinition field, ToleranceConfig config) {
        StringBuilder ann = new StringBuilder();
        if (field.getType() == FieldDefinition.FieldType.DISPLAY_SIGNED ||
                field.getType() == FieldDefinition.FieldType.COMP3) {
            ann.append("tolerance: ").append(config.getToleranceForField(field.getName()).toPlainString());
        }
        if (field.isDateField() && config.isNormalizeDates()) {
            if (!ann.isEmpty()) ann.append(", ");
            ann.append("date normalized");
        }
        if (field.getType() == FieldDefinition.FieldType.FILLER && config.isIgnoreFiller()) {
            ann.append("filler skipped");
        }
        return ann.isEmpty() ? null : ann.toString();
    }

    private static byte[] readAllBytes(Path path) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            byte[] data = new byte[(int) raf.length()];
            raf.readFully(data);
            return data;
        }
    }
}
