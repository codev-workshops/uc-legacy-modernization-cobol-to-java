package com.carddemo.batch.job;

import com.carddemo.common.entity.Card;
import org.springframework.batch.item.file.transform.LineAggregator;

/**
 * Formats a Card entity into the fixed-width 150-character COBOL record layout
 * defined in copybook CVACT02Y:
 *   CARD-NUM            PIC X(16)
 *   CARD-ACCT-ID        PIC 9(11)
 *   CARD-CVV-CD         PIC 9(03)
 *   CARD-EMBOSSED-NAME  PIC X(50)
 *   CARD-EXPIRAION-DATE PIC X(10)
 *   CARD-ACTIVE-STATUS  PIC X(01)
 *   FILLER              PIC X(59)
 */
public class CardRecordLineAggregator implements LineAggregator<Card> {

    private static final int CARD_NUM_LEN = 16;
    private static final int ACCT_ID_LEN = 11;
    private static final int CVV_CD_LEN = 3;
    private static final int EMBOSSED_NAME_LEN = 50;
    private static final int EXPIRATION_DATE_LEN = 10;
    private static final int ACTIVE_STATUS_LEN = 1;
    private static final int FILLER_LEN = 59;

    @Override
    public String aggregate(Card card) {
        StringBuilder sb = new StringBuilder(150);
        sb.append(padRight(card.getCardNum(), CARD_NUM_LEN));
        sb.append(padLeftZeros(card.getAcctId(), ACCT_ID_LEN));
        sb.append(padLeftZeros(card.getCvvCd(), CVV_CD_LEN));
        sb.append(padRight(card.getEmbossedName(), EMBOSSED_NAME_LEN));
        sb.append(padRight(card.getExpirationDate(), EXPIRATION_DATE_LEN));
        sb.append(padRight(card.getActiveStatus(), ACTIVE_STATUS_LEN));
        sb.append(padRight("", FILLER_LEN));
        return sb.toString();
    }

    private String padRight(String value, int length) {
        if (value == null) {
            value = "";
        }
        if (value.length() >= length) {
            return value.substring(0, length);
        }
        return String.format("%-" + length + "s", value);
    }

    private String padLeftZeros(Object value, int length) {
        String str = (value == null) ? "0" : String.valueOf(value);
        if (str.length() >= length) {
            return str.substring(str.length() - length);
        }
        return String.format("%" + length + "s", str).replace(' ', '0');
    }
}
