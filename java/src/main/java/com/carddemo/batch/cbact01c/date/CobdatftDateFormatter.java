package com.carddemo.batch.cbact01c.date;

import java.util.Arrays;

/**
 * Faithful port of {@code app/asm/COBDATFT.asm}.
 *
 * <p>The output area ({@code COOUTDT}, 20 bytes) starts as {@link #FILL} bytes and the error
 * message ({@code COERMSG}, 38 bytes) as spaces. Only the bytes the assembler {@code MVC}/{@code MVI}
 * instructions target are written; everything else is left as initialised.
 *
 * <pre>
 * COINTYPE '1' (YYYYMMDD):  COINPDT+4 == '-'      -> error
 *                           COOUTYPE == '2'        -> error
 *                           else out[0..3]=in[0..3], out[4]='-', out[5..6]=in[4..5],
 *                                out[7]='-', out[8..9]=in[6..7]
 * COINTYPE '2' (YYYY-MM-DD): COOUTYPE == '1'       -> error (separator check is commented out)
 *                           else out[0..3]=in[0..3], out[4..5]=in[5..6], out[6..7]=in[8..9]
 * any other COINTYPE                               -> error
 * error: COERMSG = "INVALID INPUT" left-justified, space padded; COOUTDT untouched
 * </pre>
 */
public final class CobdatftDateFormatter implements DateFormatter {

    /** Initial content of {@code CODATECN-0UT-DATE}; the copybook has no VALUE clause (see design §6). */
    public static final char FILL = ' ';

    public static final String INVALID_INPUT = "INVALID INPUT";

    private static final char TYPE_YYYYMMDD = '1';
    private static final char TYPE_YYYY_MM_DD = '2';
    private static final char SEPARATOR = '-';

    @Override
    public DateConversionResponse convert(DateConversionRequest request) {
        char[] in = request.inputDate().toCharArray();
        char[] out = new char[DateConversionResponse.OUTPUT_DATE_LENGTH];
        Arrays.fill(out, FILL);
        boolean error;

        if (request.inType() == TYPE_YYYYMMDD) {
            error = validIn1(in, request.outType(), out);
        } else if (request.inType() == TYPE_YYYY_MM_DD) {
            error = validIn2(in, request.outType(), out);
        } else {
            error = true;
        }

        String errorMessage = error ? INVALID_INPUT : "";
        return new DateConversionResponse(new String(out), errorMessage);
    }

    /** VALIDIN1: YYYYMMDD -> YYYY-MM-DD. Returns {@code true} on GOTOERR. */
    private static boolean validIn1(char[] in, char outType, char[] out) {
        if (in[4] == SEPARATOR) {
            return true;
        }
        if (outType == TYPE_YYYY_MM_DD) {
            return true;
        }
        System.arraycopy(in, 0, out, 0, 4);
        out[4] = SEPARATOR;
        System.arraycopy(in, 4, out, 5, 2);
        out[7] = SEPARATOR;
        System.arraycopy(in, 6, out, 8, 2);
        return false;
    }

    /** VALIDIN2: YYYY-MM-DD -> YYYYMMDD. Returns {@code true} on GOTOERR. */
    private static boolean validIn2(char[] in, char outType, char[] out) {
        if (outType == TYPE_YYYYMMDD) {
            return true;
        }
        System.arraycopy(in, 0, out, 0, 4);
        System.arraycopy(in, 5, out, 4, 2);
        System.arraycopy(in, 8, out, 6, 2);
        return false;
    }
}
