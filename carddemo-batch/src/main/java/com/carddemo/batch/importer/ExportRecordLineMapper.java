package com.carddemo.batch.importer;

import com.carddemo.batch.export.ExportRecord;
import com.carddemo.batch.export.RecordType;
import org.springframework.batch.item.file.LineMapper;

import java.util.Arrays;

/**
 * Parses a pipe-delimited line from the export file back into an {@link ExportRecord}.
 * Inverse of {@link com.carddemo.batch.export.ExportRecordLineAggregator}.
 */
public class ExportRecordLineMapper implements LineMapper<ExportRecord> {

    private static final String DELIMITER = "\\|";
    private static final int MIN_HEADER_FIELDS = 5;

    @Override
    public ExportRecord mapLine(String line, int lineNumber) throws Exception {
        String[] tokens = line.split(DELIMITER, -1);
        if (tokens.length < MIN_HEADER_FIELDS) {
            throw new IllegalArgumentException(
                    "Invalid export record at line " + lineNumber
                            + ": expected at least " + MIN_HEADER_FIELDS + " fields, got " + tokens.length);
        }

        ExportRecord record = new ExportRecord();
        record.setRecordType(RecordType.fromCode(tokens[0].charAt(0)));
        record.setTimestamp(tokens[1]);
        record.setSequenceNum(Long.parseLong(tokens[2]));
        record.setBranchId(tokens[3]);
        record.setRegionCode(tokens[4]);

        if (tokens.length > MIN_HEADER_FIELDS) {
            record.setFields(Arrays.copyOfRange(tokens, MIN_HEADER_FIELDS, tokens.length));
        } else {
            record.setFields(new String[0]);
        }
        return record;
    }
}
