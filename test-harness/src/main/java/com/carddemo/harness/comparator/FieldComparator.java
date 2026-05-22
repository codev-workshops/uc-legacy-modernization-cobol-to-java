package com.carddemo.harness.comparator;

import com.carddemo.harness.codec.PackedDecimalCodec;
import com.carddemo.harness.codec.ZonedDecimalCodec;
import com.carddemo.harness.config.ToleranceConfig;
import com.carddemo.harness.parser.FieldDefinition;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Compares two field byte arrays based on their COBOL type and tolerance settings.
 */
public class FieldComparator {

    public CompareResult compare(byte[] cobolField, byte[] javaField,
                                  FieldDefinition def, ToleranceConfig config) {
        return switch (def.getType()) {
            case ALPHANUMERIC -> compareAlphanumeric(cobolField, javaField, def, config);
            case DISPLAY_NUMERIC -> compareDisplayNumeric(cobolField, javaField, def, config);
            case DISPLAY_SIGNED -> compareDisplaySigned(cobolField, javaField, def, config);
            case COMP3 -> compareComp3(cobolField, javaField, def, config);
            case FILLER -> config.isIgnoreFiller() ? CompareResult.SKIPPED
                    : (Arrays.equals(cobolField, javaField) ? CompareResult.MATCH : CompareResult.MISMATCH);
        };
    }

    private CompareResult compareAlphanumeric(byte[] cobolField, byte[] javaField,
                                               FieldDefinition def, ToleranceConfig config) {
        String cobolStr = new String(cobolField, config.getCobolEncoding());
        String javaStr = new String(javaField, config.getCobolEncoding());

        if (config.isRtrimAlphanumeric()) {
            cobolStr = rtrim(cobolStr);
            javaStr = rtrim(javaStr);
        }

        if (def.isDateField() && config.isNormalizeDates()) {
            cobolStr = normalizeDateToYYYYMMDD(cobolStr);
            javaStr = normalizeDateToYYYYMMDD(javaStr);
        }

        return cobolStr.equals(javaStr) ? CompareResult.MATCH : CompareResult.MISMATCH;
    }

    private CompareResult compareDisplayNumeric(byte[] cobolField, byte[] javaField,
                                                 FieldDefinition def, ToleranceConfig config) {
        String cobolStr = new String(cobolField, config.getCobolEncoding()).trim();
        String javaStr = new String(javaField, config.getCobolEncoding()).trim();
        return cobolStr.equals(javaStr) ? CompareResult.MATCH : CompareResult.MISMATCH;
    }

    private CompareResult compareDisplaySigned(byte[] cobolField, byte[] javaField,
                                                FieldDefinition def, ToleranceConfig config) {
        BigDecimal cobolVal = ZonedDecimalCodec.decode(cobolField, def.getScale());
        BigDecimal javaVal = ZonedDecimalCodec.decode(javaField, def.getScale());
        return cobolVal.subtract(javaVal).abs().compareTo(config.getNumericTolerance()) <= 0
                ? CompareResult.MATCH : CompareResult.MISMATCH;
    }

    private CompareResult compareComp3(byte[] cobolField, byte[] javaField,
                                        FieldDefinition def, ToleranceConfig config) {
        BigDecimal cobolPacked = PackedDecimalCodec.decode(cobolField, def.getScale());
        BigDecimal javaPacked = PackedDecimalCodec.decode(javaField, def.getScale());
        return cobolPacked.subtract(javaPacked).abs().compareTo(config.getNumericTolerance()) <= 0
                ? CompareResult.MATCH : CompareResult.MISMATCH;
    }

    static String rtrim(String s) {
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == ' ') {
            end--;
        }
        return s.substring(0, end);
    }

    static String normalizeDateToYYYYMMDD(String rawDate) {
        if (rawDate == null) {
            return "";
        }
        // Strip separators and take first 8 meaningful characters
        String cleaned = rawDate.replace("-", "").replace("/", "").replace(".", "");
        if (cleaned.length() >= 8) {
            return cleaned.substring(0, 8);
        }
        return cleaned;
    }
}
