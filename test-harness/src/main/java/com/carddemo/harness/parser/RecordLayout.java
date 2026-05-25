package com.carddemo.harness.parser;

import com.carddemo.harness.parser.FieldDefinition.FieldType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hard-coded record layouts derived from COBOL copybooks and FD sections.
 * Also supports loading layouts from JSON resource files.
 */
public final class RecordLayout {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String name;
    private final int recordLength;
    private final List<FieldDefinition> fields;

    public RecordLayout(String name, int recordLength, List<FieldDefinition> fields) {
        this.name = name;
        this.recordLength = recordLength;
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
    }

    public String getName() {
        return name;
    }

    public int getRecordLength() {
        return recordLength;
    }

    public List<FieldDefinition> getFields() {
        return fields;
    }

    // ------------------------------------------------------------------
    // Hard-coded layouts
    // ------------------------------------------------------------------

    /**
     * OUTFILE layout from CBACT01C FD OUT-FILE (lines 57-69).
     * Total: 11+1+12+12+12+10+10+10+12+7+10 = 107 bytes
     */
    public static RecordLayout outfileLayout() {
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(new FieldDefinition("OUT-ACCT-ID", 0, 11, FieldType.DISPLAY_NUMERIC));
        fields.add(new FieldDefinition("OUT-ACCT-ACTIVE-STATUS", 11, 1, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("OUT-ACCT-CURR-BAL", 12, 12, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("OUT-ACCT-CREDIT-LIMIT", 24, 12, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("OUT-ACCT-CASH-CREDIT-LIMIT", 36, 12, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("OUT-ACCT-OPEN-DATE", 48, 10, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("OUT-ACCT-EXPIRAION-DATE", 58, 10, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("OUT-ACCT-REISSUE-DATE", 68, 10, FieldType.ALPHANUMERIC, 0, true));
        fields.add(new FieldDefinition("OUT-ACCT-CURR-CYC-CREDIT", 78, 12, FieldType.DISPLAY_SIGNED, 2));
        // PIC S9(10)V99 COMP-3 => 12 digits + sign => 7 bytes
        fields.add(new FieldDefinition("OUT-ACCT-CURR-CYC-DEBIT", 90, 7, FieldType.COMP3, 2));
        fields.add(new FieldDefinition("OUT-ACCT-GROUP-ID", 97, 10, FieldType.ALPHANUMERIC));
        return new RecordLayout("OUTFILE", 107, fields);
    }

    /**
     * ACCOUNT-RECORD layout from CVACT01Y.cpy: 300 bytes total, all DISPLAY.
     */
    public static RecordLayout accountRecordLayout() {
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(new FieldDefinition("ACCT-ID", 0, 11, FieldType.DISPLAY_NUMERIC));
        fields.add(new FieldDefinition("ACCT-ACTIVE-STATUS", 11, 1, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("ACCT-CURR-BAL", 12, 12, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("ACCT-CREDIT-LIMIT", 24, 12, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("ACCT-CASH-CREDIT-LIMIT", 36, 12, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("ACCT-OPEN-DATE", 48, 10, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("ACCT-EXPIRAION-DATE", 58, 10, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("ACCT-REISSUE-DATE", 68, 10, FieldType.ALPHANUMERIC, 0, true));
        fields.add(new FieldDefinition("ACCT-CURR-CYC-CREDIT", 78, 12, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("ACCT-CURR-CYC-DEBIT", 90, 12, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("ACCT-ADDR-ZIP", 102, 10, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("ACCT-GROUP-ID", 112, 10, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("FILLER", 122, 178, FieldType.FILLER));
        return new RecordLayout("ACCOUNT-RECORD", 300, fields);
    }

    /**
     * TRAN-RECORD layout from CVTRA05Y.cpy: 350 bytes total.
     * TRAN-AMT is PIC S9(09)V99 at offset 132, length 11.
     */
    public static RecordLayout tranRecordLayout() {
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(new FieldDefinition("TRAN-ID", 0, 16, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("TRAN-TYPE-CD", 16, 2, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("TRAN-CAT-CD", 18, 4, FieldType.DISPLAY_NUMERIC));
        fields.add(new FieldDefinition("TRAN-SOURCE", 22, 10, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("TRAN-DESC", 32, 100, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("TRAN-AMT", 132, 11, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("TRAN-MERCHANT-ID", 143, 9, FieldType.DISPLAY_NUMERIC));
        fields.add(new FieldDefinition("TRAN-MERCHANT-NAME", 152, 50, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("TRAN-MERCHANT-CITY", 202, 50, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("TRAN-MERCHANT-ZIP", 252, 10, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("TRAN-CARD-NUM", 262, 16, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("TRAN-ORIG-TS", 278, 26, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("TRAN-PROC-TS", 304, 26, FieldType.ALPHANUMERIC));
        fields.add(new FieldDefinition("FILLER", 330, 20, FieldType.FILLER));
        return new RecordLayout("TRAN-RECORD", 350, fields);
    }

    /**
     * ARRY-FILE layout from CBACT01C FD (lines 72-78).
     * ARR-ACCT-ID: 11 bytes unsigned
     * ARR-ACCT-BAL OCCURS 5: each = 12 bytes DISPLAY_SIGNED + 7 bytes COMP-3 = 19 bytes
     * ARR-FILLER: 4 bytes
     * Total: 11 + (5 * 19) + 4 = 110 bytes
     */
    public static RecordLayout arryFileLayout() {
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(new FieldDefinition("ARR-ACCT-ID", 0, 11, FieldType.DISPLAY_NUMERIC));
        int base = 11;
        for (int i = 1; i <= 5; i++) {
            fields.add(new FieldDefinition("ARR-ACCT-CURR-BAL(" + i + ")", base, 12,
                    FieldType.DISPLAY_SIGNED, 2));
            fields.add(new FieldDefinition("ARR-ACCT-CURR-CYC-DEBIT(" + i + ")", base + 12, 7,
                    FieldType.COMP3, 2));
            base += 19;
        }
        fields.add(new FieldDefinition("ARR-FILLER", base, 4, FieldType.FILLER));
        return new RecordLayout("ARRY-FILE", 110, fields);
    }

    /**
     * VBRC-FILE layout from CBACT01C.
     * Variable-length: two record types.
     * REC1 (12 bytes): VB1-ACCT-ID (11, DISPLAY_NUMERIC) + VB1-ACCT-ACTIVE-STATUS (1, ALPHA)
     * REC2 (39 bytes): VB2-ACCT-ID (11, DISPLAY_NUMERIC) + VB2-ACCT-CURR-BAL (12, DISPLAY_SIGNED, scale 2)
     *                  + VB2-ACCT-CREDIT-LIMIT (12, DISPLAY_SIGNED, scale 2) + VB2-ACCT-REISSUE-YYYY (4, ALPHA)
     */
    public static RecordLayout vbrcFileRec1Layout() {
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(new FieldDefinition("VB1-ACCT-ID", 0, 11, FieldType.DISPLAY_NUMERIC));
        fields.add(new FieldDefinition("VB1-ACCT-ACTIVE-STATUS", 11, 1, FieldType.ALPHANUMERIC));
        return new RecordLayout("VBRC-REC1", 12, fields);
    }

    public static RecordLayout vbrcFileRec2Layout() {
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(new FieldDefinition("VB2-ACCT-ID", 0, 11, FieldType.DISPLAY_NUMERIC));
        fields.add(new FieldDefinition("VB2-ACCT-CURR-BAL", 11, 12, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("VB2-ACCT-CREDIT-LIMIT", 23, 12, FieldType.DISPLAY_SIGNED, 2));
        fields.add(new FieldDefinition("VB2-ACCT-REISSUE-YYYY", 35, 4, FieldType.ALPHANUMERIC));
        return new RecordLayout("VBRC-REC2", 39, fields);
    }

    // ------------------------------------------------------------------
    // JSON-based layout loading
    // ------------------------------------------------------------------

    public static RecordLayout fromJsonResource(String resourcePath, String layoutName, int recordLength)
            throws IOException {
        try (InputStream is = RecordLayout.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Layout resource not found: " + resourcePath);
            }
            List<FieldDefinition> fields = MAPPER.readValue(is, new TypeReference<List<FieldDefinition>>() {});
            return new RecordLayout(layoutName, recordLength, fields);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Layout: %s  RecordLength: %d  Fields: %d%n", name, recordLength, fields.size()));
        for (FieldDefinition f : fields) {
            sb.append("  ").append(f).append('\n');
        }
        return sb.toString();
    }
}
