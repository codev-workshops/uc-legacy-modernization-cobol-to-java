package com.carddemo.batch.export;

/**
 * Record type codes matching the CVEXPORT.cpy polymorphic export layout.
 * Each code identifies the entity type stored in the EXPORT-RECORD-DATA area.
 */
public enum RecordType {
    CUSTOMER('C'),
    ACCOUNT('A'),
    CARD_XREF('X'),
    TRANSACTION('T'),
    CARD('D'),
    TRAN_CAT_BALANCE('B');

    private final char code;

    RecordType(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }

    public static RecordType fromCode(char code) {
        for (RecordType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown record type code: " + code);
    }
}
