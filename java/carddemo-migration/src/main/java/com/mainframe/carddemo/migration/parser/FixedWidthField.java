package com.mainframe.carddemo.migration.parser;

public class FixedWidthField {

    public enum FieldType {
        ALPHANUMERIC,
        NUMERIC,
        SIGNED_NUMERIC,
        DATE,
        FILLER
    }

    private final String name;
    private final int offset;
    private final int length;
    private final FieldType type;
    private final int scale;

    public FixedWidthField(String name, int offset, int length, FieldType type) {
        this(name, offset, length, type, 0);
    }

    public FixedWidthField(String name, int offset, int length, FieldType type, int scale) {
        this.name = name;
        this.offset = offset;
        this.length = length;
        this.type = type;
        this.scale = scale;
    }

    public String getName() { return name; }
    public int getOffset() { return offset; }
    public int getLength() { return length; }
    public FieldType getType() { return type; }
    public int getScale() { return scale; }
}
