package com.mainframe.carddemo.migration.parser;

import com.mainframe.carddemo.migration.codec.ZonedDecimalCodec;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FixedWidthRecordParser {

    private final List<FixedWidthField> fields;

    public FixedWidthRecordParser(List<FixedWidthField> fields) {
        this.fields = fields;
    }

    public Map<String, Object> parse(String line) {
        Map<String, Object> record = new LinkedHashMap<>();
        for (FixedWidthField field : fields) {
            if (field.getType() == FixedWidthField.FieldType.FILLER) {
                continue;
            }
            String raw = extractField(line, field.getOffset(), field.getLength());
            record.put(field.getName(), convertField(raw, field));
        }
        return record;
    }

    private String extractField(String line, int offset, int length) {
        if (offset >= line.length()) {
            return "";
        }
        int end = Math.min(offset + length, line.length());
        return line.substring(offset, end);
    }

    private Object convertField(String raw, FixedWidthField field) {
        String trimmed = raw.trim();
        switch (field.getType()) {
            case ALPHANUMERIC:
                return trimmed;
            case NUMERIC:
                if (trimmed.isEmpty()) return 0L;
                try {
                    return Long.parseLong(trimmed);
                } catch (NumberFormatException e) {
                    return 0L;
                }
            case SIGNED_NUMERIC:
                if (trimmed.isEmpty()) return BigDecimal.ZERO;
                return ZonedDecimalCodec.decode(raw.stripLeading().isEmpty() ? raw : raw, field.getScale());
            case DATE:
                return trimmed.isEmpty() ? null : trimmed;
            default:
                return trimmed;
        }
    }
}
