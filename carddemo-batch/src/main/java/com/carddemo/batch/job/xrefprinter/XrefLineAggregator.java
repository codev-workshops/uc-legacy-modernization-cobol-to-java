package com.carddemo.batch.job.xrefprinter;

import com.carddemo.common.entity.CardXref;
import org.springframework.batch.item.file.transform.LineAggregator;

/**
 * Formats a {@link CardXref} record into the fixed-width layout defined by the
 * COBOL copybook CVACT03Y (record length 50):
 * <pre>
 *   XREF-CARD-NUM   PIC X(16)
 *   XREF-CUST-ID    PIC 9(09)
 *   XREF-ACCT-ID    PIC 9(11)
 *   FILLER           PIC X(14)
 * </pre>
 */
public class XrefLineAggregator implements LineAggregator<CardXref> {

    static final int CARD_NUM_LEN = 16;
    static final int CUST_ID_LEN = 9;
    static final int ACCT_ID_LEN = 11;
    static final int FILLER_LEN = 14;
    static final int RECORD_LEN = CARD_NUM_LEN + CUST_ID_LEN + ACCT_ID_LEN + FILLER_LEN;
    static final String FILLER = " ".repeat(FILLER_LEN);

    @Override
    public String aggregate(CardXref item) {
        StringBuilder sb = new StringBuilder(RECORD_LEN);
        sb.append(padRight(item.getXrefCardNum(), CARD_NUM_LEN));
        sb.append(zeroPad(item.getCustId(), CUST_ID_LEN));
        sb.append(zeroPad(item.getAcctId(), ACCT_ID_LEN));
        sb.append(FILLER);
        return sb.toString();
    }

    static String padRight(String s, int len) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= len) {
            return s.substring(0, len);
        }
        StringBuilder sb = new StringBuilder(len);
        sb.append(s);
        while (sb.length() < len) {
            sb.append(' ');
        }
        return sb.toString();
    }

    static String zeroPad(Long value, int len) {
        if (value == null) {
            value = 0L;
        }
        return String.format("%0" + len + "d", value);
    }
}
