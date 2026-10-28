package com.carddemo.harness.codec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Hand-transcribed storage definitions; each layout cites its source COBOL copybook. */
public final class CardDemoLayouts {
  private static final Map<String, RecordLayout> LAYOUTS = build();

  private CardDemoLayouts() {}

  public static RecordLayout get(String dataset) {
    RecordLayout layout = LAYOUTS.get(dataset.toLowerCase());
    if (layout == null) {
      throw new IllegalArgumentException("Unknown dataset " + dataset);
    }
    return layout;
  }

  public static Map<String, RecordLayout> all() {
    return LAYOUTS;
  }

  private static Map<String, RecordLayout> build() {
    Map<String, RecordLayout> layouts = new LinkedHashMap<>();
    layouts.put("acctdata", layout("ACCOUNT-RECORD", "app/cpy/CVACT01Y.cpy", 300,
        fields("11|ACCT-ID|9(11)", "1|ACCT-ACTIVE-STATUS|X(01)", "12|ACCT-CURR-BAL|S9(10)V99",
            "12|ACCT-CREDIT-LIMIT|S9(10)V99", "12|ACCT-CASH-CREDIT-LIMIT|S9(10)V99",
            "10|ACCT-OPEN-DATE|X(10)", "10|ACCT-EXPIRAION-DATE|X(10)", "10|ACCT-REISSUE-DATE|X(10)",
            "12|ACCT-CURR-CYC-CREDIT|S9(10)V99", "12|ACCT-CURR-CYC-DEBIT|S9(10)V99",
            "10|ACCT-ADDR-ZIP|X(10)", "10|ACCT-GROUP-ID|X(10)", "178|FILLER|X(178)")));
    layouts.put("carddata", layout("CARD-RECORD", "app/cpy/CVACT02Y.cpy", 150,
        fields("16|CARD-NUM|X(16)", "11|CARD-ACCT-ID|9(11)", "3|CARD-CVV-CD|9(03)",
            "50|CARD-EMBOSSED-NAME|X(50)", "10|CARD-EXPIRAION-DATE|X(10)",
            "1|CARD-ACTIVE-STATUS|X(01)", "59|FILLER|X(59)")));
    layouts.put("cardxref", layout("CARD-XREF-RECORD", "app/cpy/CVACT03Y.cpy", 50,
        fields("16|XREF-CARD-NUM|X(16)", "9|XREF-CUST-ID|9(09)", "11|XREF-ACCT-ID|9(11)",
            "14|FILLER|X(14)")));
    layouts.put("custdata", layout("CUSTOMER-RECORD", "app/cpy/CVCUS01Y.cpy", 500,
        fields("9|CUST-ID|9(09)", "25|CUST-FIRST-NAME|X(25)", "25|CUST-MIDDLE-NAME|X(25)",
            "25|CUST-LAST-NAME|X(25)", "50|CUST-ADDR-LINE-1|X(50)", "50|CUST-ADDR-LINE-2|X(50)",
            "50|CUST-ADDR-LINE-3|X(50)", "2|CUST-ADDR-STATE-CD|X(02)", "3|CUST-ADDR-COUNTRY-CD|X(03)",
            "10|CUST-ADDR-ZIP|X(10)", "15|CUST-PHONE-NUM-1|X(15)", "15|CUST-PHONE-NUM-2|X(15)",
            "9|CUST-SSN|9(09)", "20|CUST-GOVT-ISSUED-ID|X(20)", "10|CUST-DOB-YYYY-MM-DD|X(10)",
            "10|CUST-EFT-ACCOUNT-ID|X(10)", "1|CUST-PRI-CARD-HOLDER-IND|X(01)",
            "3|CUST-FICO-CREDIT-SCORE|9(03)", "168|FILLER|X(168)")));
    layouts.put("dailytran", layout("DALYTRAN-RECORD", "app/cpy/CVTRA06Y.cpy", 350,
        fields("16|DALYTRAN-ID|X(16)", "2|DALYTRAN-TYPE-CD|X(02)", "4|DALYTRAN-CAT-CD|9(04)",
            "10|DALYTRAN-SOURCE|X(10)", "100|DALYTRAN-DESC|X(100)", "11|DALYTRAN-AMT|S9(09)V99",
            "9|DALYTRAN-MERCHANT-ID|9(09)", "50|DALYTRAN-MERCHANT-NAME|X(50)",
            "50|DALYTRAN-MERCHANT-CITY|X(50)", "10|DALYTRAN-MERCHANT-ZIP|X(10)",
            "16|DALYTRAN-CARD-NUM|X(16)", "26|DALYTRAN-ORIG-TS|X(26)",
            "26|DALYTRAN-PROC-TS|X(26)", "20|FILLER|X(20)")));
    layouts.put("discgrp", grouped("DIS-GROUP-RECORD", "app/cpy/CVTRA02Y.cpy", 50,
        group("DIS-GROUP-KEY", 0, 16, fields("10|DIS-ACCT-GROUP-ID|X(10)",
            "2|DIS-TRAN-TYPE-CD|X(02)", "4|DIS-TRAN-CAT-CD|9(04)")),
        fields("6|DIS-INT-RATE|S9(04)V99", "28|FILLER|X(28)")));
    layouts.put("tcatbal", grouped("TRAN-CAT-BAL-RECORD", "app/cpy/CVTRA01Y.cpy", 50,
        group("TRAN-CAT-KEY", 0, 17, fields("11|TRANCAT-ACCT-ID|9(11)",
            "2|TRANCAT-TYPE-CD|X(02)", "4|TRANCAT-CD|9(04)")),
        fields("11|TRAN-CAT-BAL|S9(09)V99", "22|FILLER|X(22)")));
    layouts.put("trancatg", grouped("TRAN-CAT-RECORD", "app/cpy/CVTRA04Y.cpy", 60,
        group("TRAN-CAT-KEY", 0, 6, fields("2|TRAN-TYPE-CD|X(02)", "4|TRAN-CAT-CD|9(04)")),
        fields("50|TRAN-CAT-TYPE-DESC|X(50)", "4|FILLER|X(04)")));
    layouts.put("trantype", layout("TRAN-TYPE-RECORD", "app/cpy/CVTRA03Y.cpy", 60,
        fields("2|TRAN-TYPE|X(02)", "50|TRAN-TYPE-DESC|X(50)", "8|FILLER|X(08)")));
    layouts.put("export", exportLayout());
    layouts.put("tran-record", layout("TRAN-RECORD", "app/cpy/CVTRA05Y.cpy", 350,
        fields("16|TRAN-ID|X(16)", "2|TRAN-TYPE-CD|X(02)", "4|TRAN-CAT-CD|9(04)",
            "10|TRAN-SOURCE|X(10)", "100|TRAN-DESC|X(100)", "11|TRAN-AMT|S9(09)V99",
            "9|TRAN-MERCHANT-ID|9(09)", "50|TRAN-MERCHANT-NAME|X(50)",
            "50|TRAN-MERCHANT-CITY|X(50)", "10|TRAN-MERCHANT-ZIP|X(10)",
            "16|TRAN-CARD-NUM|X(16)", "26|TRAN-ORIG-TS|X(26)",
            "26|TRAN-PROC-TS|X(26)", "20|FILLER|X(20)")));
    layouts.put("dalyrejs", layout("REJECT-RECORD", "app/cbl/CBTRN02C.cbl", 430,
        fields("350|REJECT-TRAN-DATA|X(350)", "4|WS-VALIDATION-FAIL-REASON|9(04)",
            "76|WS-VALIDATION-FAIL-REASON-DESC|X(76)")));
    return Map.copyOf(layouts);
  }

  private static List<FieldDef> fields(String... definitions) {
    List<FieldDef> fields = new ArrayList<>();
    int offset = 0;
    for (String definition : definitions) {
      String[] parts = definition.split("\\|");
      int length = Integer.parseInt(parts[0]);
      Pic pic = Pic.parse(parts[2]);
      fields.add(new FieldDef(parts[1], offset, length, pic, Usage.DISPLAY, 1, List.of(), null,
          "FILLER".equals(parts[1])));
      offset += length;
    }
    return fields;
  }

  private static RecordLayout layout(String name, String copybook, int length, List<FieldDef> fields) {
    return new RecordLayout(name, copybook, length, fields, Optional.empty());
  }

  private static RecordLayout grouped(String name, String copybook, int length, FieldDef group,
      List<FieldDef> tail) {
    List<FieldDef> fields = new ArrayList<>();
    fields.add(group);
    fields.addAll(withOffset(tail, group.offset() + group.length()));
    return layout(name, copybook, length, fields);
  }

  private static FieldDef group(String name, int offset, int length, List<FieldDef> children) {
    return FieldDef.group(name, offset, length, 1, withOffset(children, offset));
  }

  private static List<FieldDef> withOffset(List<FieldDef> fields, int start) {
    List<FieldDef> result = new ArrayList<>();
    int offset = start;
    for (FieldDef field : fields) {
      result.add(new FieldDef(field.name(), offset, field.length(), field.pic(), field.usage(),
          field.occurs(), field.children(), field.redefines(), field.filler()));
      offset += field.length() * field.occurs();
    }
    return result;
  }

  private static RecordLayout exportLayout() {
    List<FieldDef> fields = new ArrayList<>(fields("1|EXPORT-REC-TYPE|X(1)",
        "26|EXPORT-TIMESTAMP|X(26)", "4|EXPORT-SEQUENCE-NUM|9(9)", "4|EXPORT-BRANCH-ID|X(4)",
        "5|EXPORT-REGION-CODE|X(5)"));
    fields.set(2, withUsage(fields.get(2), Usage.COMP));
    fields.add(2, redefines(FieldDef.group("EXPORT-TIMESTAMP-R", 1, 26, 1, List.of(
        FieldDef.elementary("EXPORT-DATE", 1, 10, "X(10)", Usage.DISPLAY),
        FieldDef.elementary("EXPORT-DATE-TIME-SEP", 11, 1, "X(1)", Usage.DISPLAY),
        FieldDef.elementary("EXPORT-TIME", 12, 15, "X(15)", Usage.DISPLAY))),
        "EXPORT-TIMESTAMP"));
    fields.add(new FieldDef("EXPORT-RECORD-DATA", 40, 460, Pic.parse("X(460)"), Usage.DISPLAY,
        1, List.of(), null, false));
    int payload = 40;
    FieldDef customer = branch("EXPORT-CUSTOMER-DATA", payload, "EXP-CUST-ID|4|9(09)|COMP",
        "EXP-CUST-FIRST-NAME|25|X(25)|DISPLAY", "EXP-CUST-MIDDLE-NAME|25|X(25)|DISPLAY",
        "EXP-CUST-LAST-NAME|25|X(25)|DISPLAY", "EXP-CUST-ADDR-LINES|150|X(50)|DISPLAY|3",
        "EXP-CUST-ADDR-STATE-CD|2|X(02)|DISPLAY", "EXP-CUST-ADDR-COUNTRY-CD|3|X(03)|DISPLAY",
        "EXP-CUST-ADDR-ZIP|10|X(10)|DISPLAY",
        "EXP-CUST-PHONE-NUMS|30|X(15)|DISPLAY|2", "EXP-CUST-SSN|9|9(09)|DISPLAY",
        "EXP-CUST-GOVT-ISSUED-ID|20|X(20)|DISPLAY", "EXP-CUST-DOB-YYYY-MM-DD|10|X(10)|DISPLAY",
        "EXP-CUST-EFT-ACCOUNT-ID|10|X(10)|DISPLAY", "EXP-CUST-PRI-CARD-HOLDER-IND|1|X(01)|DISPLAY",
        "EXP-CUST-FICO-CREDIT-SCORE|2|9(03)|COMP_3", "FILLER|134|X(134)|DISPLAY");
    FieldDef account = branch("EXPORT-ACCOUNT-DATA", payload, "EXP-ACCT-ID|11|9(11)|DISPLAY",
        "EXP-ACCT-ACTIVE-STATUS|1|X(01)|DISPLAY", "EXP-ACCT-CURR-BAL|7|S9(10)V99|COMP_3",
        "EXP-ACCT-CREDIT-LIMIT|12|S9(10)V99|DISPLAY", "EXP-ACCT-CASH-CREDIT-LIMIT|7|S9(10)V99|COMP_3",
        "EXP-ACCT-OPEN-DATE|10|X(10)|DISPLAY", "EXP-ACCT-EXPIRAION-DATE|10|X(10)|DISPLAY",
        "EXP-ACCT-REISSUE-DATE|10|X(10)|DISPLAY", "EXP-ACCT-CURR-CYC-CREDIT|12|S9(10)V99|DISPLAY",
        "EXP-ACCT-CURR-CYC-DEBIT|8|S9(10)V99|COMP", "EXP-ACCT-ADDR-ZIP|10|X(10)|DISPLAY",
        "EXP-ACCT-GROUP-ID|10|X(10)|DISPLAY", "FILLER|352|X(352)|DISPLAY");
    FieldDef xref = branch("EXPORT-CARD-XREF-DATA", payload, "EXP-XREF-CARD-NUM|16|X(16)|DISPLAY",
        "EXP-XREF-CUST-ID|9|9(09)|DISPLAY", "EXP-XREF-ACCT-ID|8|9(11)|COMP",
        "FILLER|427|X(427)|DISPLAY");
    FieldDef transaction = branch("EXPORT-TRANSACTION-DATA", payload,
        "EXP-TRAN-ID|16|X(16)|DISPLAY", "EXP-TRAN-TYPE-CD|2|X(02)|DISPLAY",
        "EXP-TRAN-CAT-CD|4|9(04)|DISPLAY", "EXP-TRAN-SOURCE|10|X(10)|DISPLAY",
        "EXP-TRAN-DESC|100|X(100)|DISPLAY", "EXP-TRAN-AMT|6|S9(09)V99|COMP_3",
        "EXP-TRAN-MERCHANT-ID|4|9(09)|COMP", "EXP-TRAN-MERCHANT-NAME|50|X(50)|DISPLAY",
        "EXP-TRAN-MERCHANT-CITY|50|X(50)|DISPLAY", "EXP-TRAN-MERCHANT-ZIP|10|X(10)|DISPLAY",
        "EXP-TRAN-CARD-NUM|16|X(16)|DISPLAY", "EXP-TRAN-ORIG-TS|26|X(26)|DISPLAY",
        "EXP-TRAN-PROC-TS|26|X(26)|DISPLAY", "FILLER|140|X(140)|DISPLAY");
    FieldDef card = branch("EXPORT-CARD-DATA", payload, "EXP-CARD-NUM|16|X(16)|DISPLAY",
        "EXP-CARD-ACCT-ID|8|9(11)|COMP", "EXP-CARD-CVV-CD|2|9(03)|COMP",
        "EXP-CARD-EMBOSSED-NAME|50|X(50)|DISPLAY", "EXP-CARD-EXPIRAION-DATE|10|X(10)|DISPLAY",
        "EXP-CARD-ACTIVE-STATUS|1|X(01)|DISPLAY", "FILLER|373|X(373)|DISPLAY");
    fields.add(redefines(customer, "EXPORT-RECORD-DATA"));
    fields.add(redefines(account, "EXPORT-RECORD-DATA"));
    fields.add(redefines(transaction, "EXPORT-RECORD-DATA"));
    fields.add(redefines(xref, "EXPORT-RECORD-DATA"));
    fields.add(redefines(card, "EXPORT-RECORD-DATA"));
    return new RecordLayout("EXPORT-RECORD", "app/cpy/CVEXPORT.cpy", 500, fields,
        Optional.of(new Discriminator("EXPORT-REC-TYPE", Map.of("C", customer.name(),
            "A", account.name(), "X", xref.name(), "T", transaction.name(), "D", card.name()))));
  }

  private static FieldDef branch(String name, int base, String... definitions) {
    List<FieldDef> children = new ArrayList<>();
    int offset = base;
    for (String definition : definitions) {
      String[] parts = definition.split("\\|");
      String fieldName = parts[0];
      int length = Integer.parseInt(parts[1]);
      Pic pic = Pic.parse(parts[2]);
      Usage usage = Usage.valueOf(parts[3]);
      int occurs = parts.length > 4 ? Integer.parseInt(parts[4]) : 1;
      if (occurs > 1) {
        List<FieldDef> occurrenceFields = List.of(new FieldDef(
            fieldName.endsWith("LINES") ? "EXP-CUST-ADDR-LINE" : "EXP-CUST-PHONE-NUM",
            offset, length / occurs, pic, usage, 1, List.of(), null, false));
        children.add(FieldDef.group(fieldName, offset, length / occurs, occurs, occurrenceFields));
      } else {
        children.add(new FieldDef(fieldName, offset, length, pic, usage, 1, List.of(), null,
            "FILLER".equals(fieldName)));
      }
      offset += length;
    }
    return FieldDef.group(name, base, 460, 1, children);
  }

  private static FieldDef redefines(FieldDef field, String target) {
    return new FieldDef(field.name(), field.offset(), field.length(), field.pic(), field.usage(),
        field.occurs(), field.children(), target, field.filler());
  }

  private static FieldDef withUsage(FieldDef field, Usage usage) {
    return new FieldDef(field.name(), field.offset(), field.length(), field.pic(), usage, field.occurs(),
        field.children(), field.redefines(), field.filler());
  }
}
