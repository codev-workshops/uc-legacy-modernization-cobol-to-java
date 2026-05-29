package com.carddemo.harness.parser;

import com.carddemo.harness.codec.ZonedDecimalCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads fixed-width COBOL data files based on copybook PIC clause definitions
 * and produces structured Map representations (suitable for JSON serialization).
 */
public class DataFileParser {

    private static final Logger LOG = LoggerFactory.getLogger(DataFileParser.class);

    private final RecordLayout layout;

    public DataFileParser(RecordLayout layout) {
        this.layout = layout;
    }

    /**
     * Parse all records from a fixed-width text file.
     * Lines shorter than the record length are right-padded with spaces.
     */
    public List<Map<String, Object>> parseFile(Path filePath) throws IOException {
        List<Map<String, Object>> records = new ArrayList<>();
        int recNum = 0;
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.US_ASCII)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                recNum++;
                // Pad to full record length
                if (line.length() < layout.getRecordLength()) {
                    line = String.format("%-" + layout.getRecordLength() + "s", line);
                }
                records.add(parseRecord(line, recNum));
            }
        }
        LOG.info("Parsed {} records from {} using layout {}", recNum, filePath, layout.getName());
        return records;
    }

    /**
     * Parse a single fixed-width record line into a field map.
     */
    public Map<String, Object> parseRecord(String line, int recordNumber) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("_record_number", recordNumber);

        for (FieldDefinition field : layout.getFields()) {
            int start = field.getOffset();
            int end = Math.min(start + field.getLength(), line.length());
            String rawValue = (start < line.length()) ? line.substring(start, end) : "";

            record.put(field.getName(), decodeField(rawValue, field));
        }
        return record;
    }

    /**
     * Decode a raw fixed-width string value according to the field type.
     */
    public static String decodeField(String rawValue, FieldDefinition field) {
        if (rawValue.isEmpty()) {
            return "";
        }
        return switch (field.getType()) {
            case ALPHANUMERIC -> rtrim(rawValue);
            case DISPLAY_NUMERIC -> rawValue.strip().isEmpty() ? "0" : rawValue.strip();
            case DISPLAY_SIGNED -> decodeZonedDecimal(rawValue, field.getScale());
            case COMP3 -> "(binary)";
            case FILLER -> rtrim(rawValue);
        };
    }

    /**
     * Decode an ASCII zoned-decimal field with trailing sign overpunch.
     */
    private static String decodeZonedDecimal(String raw, int scale) {
        if (raw == null || raw.isBlank()) {
            return "0";
        }

        byte[] bytes = raw.getBytes(StandardCharsets.US_ASCII);
        BigDecimal value = ZonedDecimalCodec.decode(bytes, scale);
        return value.toPlainString();
    }

    /**
     * Extract the value of a named field from a parsed record.
     */
    public static String fieldValue(Map<String, Object> record, String fieldName) {
        Object val = record.get(fieldName);
        return val != null ? val.toString() : "";
    }

    /**
     * Extract a numeric field value as BigDecimal.
     */
    public static BigDecimal numericFieldValue(Map<String, Object> record, String fieldName) {
        String val = fieldValue(record, fieldName);
        if (val.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(val);
    }

    /**
     * Write parsed records to a JSON file.
     */
    public static void writeJson(List<Map<String, Object>> records, Path outputPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(outputPath.toFile(), records);
    }

    private static String rtrim(String s) {
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == ' ') {
            end--;
        }
        return s.substring(0, end);
    }
}
