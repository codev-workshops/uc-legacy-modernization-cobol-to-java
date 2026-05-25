package com.carddemo.harness.parser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single COBOL field within a record layout.
 */
public class FieldDefinition {

    public enum FieldType {
        DISPLAY_NUMERIC,
        DISPLAY_SIGNED,
        COMP3,
        ALPHANUMERIC,
        FILLER
    }

    private final String name;
    private final int offset;
    private final int length;
    private final FieldType type;
    private final int scale;
    private final boolean dateField;
    private final int occurrences;

    @JsonCreator
    public FieldDefinition(
            @JsonProperty("name") String name,
            @JsonProperty("offset") int offset,
            @JsonProperty("length") int length,
            @JsonProperty("type") FieldType type,
            @JsonProperty("scale") int scale,
            @JsonProperty("dateField") boolean dateField,
            @JsonProperty("occurrences") int occurrences) {
        this.name = name;
        this.offset = offset;
        this.length = length;
        this.type = type;
        this.scale = scale;
        this.dateField = dateField;
        this.occurrences = occurrences <= 0 ? 1 : occurrences;
    }

    public FieldDefinition(String name, int offset, int length, FieldType type, int scale, boolean dateField) {
        this(name, offset, length, type, scale, dateField, 1);
    }

    public FieldDefinition(String name, int offset, int length, FieldType type, int scale) {
        this(name, offset, length, type, scale, false, 1);
    }

    public FieldDefinition(String name, int offset, int length, FieldType type) {
        this(name, offset, length, type, 0, false, 1);
    }

    /**
     * For COMP-3 fields, calculate byte length from digit count.
     * PIC S9(n)V99 => total digits = n+2, byte length = (digits+1)/2
     */
    public static int comp3ByteLength(int totalDigits) {
        return (totalDigits + 1) / 2;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public FieldType getType() {
        return type;
    }

    public int getScale() {
        return scale;
    }

    public boolean isDateField() {
        return dateField;
    }

    public int getOccurrences() {
        return occurrences;
    }

    /**
     * Total bytes consumed by this field (accounting for OCCURS).
     */
    public int totalBytes() {
        return length * occurrences;
    }

    @Override
    public String toString() {
        return String.format("%-30s offset=%3d len=%3d type=%-16s scale=%d date=%b occ=%d",
                name, offset, length, type, scale, dateField, occurrences);
    }
}
