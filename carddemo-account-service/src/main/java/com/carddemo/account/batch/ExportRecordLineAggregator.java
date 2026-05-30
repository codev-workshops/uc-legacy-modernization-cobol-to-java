package com.carddemo.account.batch;

import com.carddemo.common.dto.ExportRecordDto;
import org.springframework.batch.item.file.transform.LineAggregator;

/**
 * Custom LineAggregator that formats ExportRecordDto into fixed-width
 * 300-character records per the CVEXPORT.cpy copybook structure.
 * <p>
 * Format: [type(1)][data(299)] = 300 bytes total.
 * The type code indicates the record kind:
 * 'A' = Account, 'C' = Card, 'U' = Customer, 'X' = CardXref
 */
public class ExportRecordLineAggregator implements LineAggregator<ExportRecordDto> {

    static final int RECORD_LENGTH = 300;

    @Override
    public String aggregate(ExportRecordDto item) {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(item.getRecordType() != null ? item.getRecordType() : " ");
        sb.append(item.getRecordData() != null ? item.getRecordData() : "");
        int remaining = RECORD_LENGTH - sb.length();
        if (remaining > 0) {
            sb.append(" ".repeat(remaining));
        }
        return sb.substring(0, RECORD_LENGTH);
    }
}
