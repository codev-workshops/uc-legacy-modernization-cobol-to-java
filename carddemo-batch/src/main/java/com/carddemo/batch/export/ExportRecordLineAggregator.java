package com.carddemo.batch.export;

import org.springframework.batch.item.file.transform.LineAggregator;

/**
 * Converts an {@link ExportRecord} to a pipe-delimited line for the export file.
 * Format: TYPE|timestamp|seqNum|branchId|regionCode|field1|field2|...
 */
public class ExportRecordLineAggregator implements LineAggregator<ExportRecord> {

    static final String DELIMITER = "|";

    @Override
    public String aggregate(ExportRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getRecordType().getCode());
        sb.append(DELIMITER).append(nullSafe(record.getTimestamp()));
        sb.append(DELIMITER).append(record.getSequenceNum());
        sb.append(DELIMITER).append(nullSafe(record.getBranchId()));
        sb.append(DELIMITER).append(nullSafe(record.getRegionCode()));
        if (record.getFields() != null) {
            for (String field : record.getFields()) {
                sb.append(DELIMITER).append(nullSafe(field));
            }
        }
        return sb.toString();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
