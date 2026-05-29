package com.carddemo.harness.config;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Tolerance rules for COBOL-vs-Java output comparison.
 */
public class ToleranceConfig {

    public enum SignConvention {
        COBOL_OVERPUNCH,
        LEADING_SIGN,
        TRAILING_SIGN
    }

    private BigDecimal numericTolerance = BigDecimal.ZERO;
    private Map<String, BigDecimal> fieldTolerances = new HashMap<>();
    private boolean rtrimAlphanumeric = true;
    private boolean ignoreFiller = true;
    private boolean stripRdw = true;
    private boolean normalizeDates = true;
    private SignConvention signConvention = SignConvention.COBOL_OVERPUNCH;
    private Charset cobolEncoding = StandardCharsets.US_ASCII;

    public BigDecimal getNumericTolerance() {
        return numericTolerance;
    }

    public void setNumericTolerance(BigDecimal numericTolerance) {
        this.numericTolerance = numericTolerance;
    }

    public Map<String, BigDecimal> getFieldTolerances() {
        return fieldTolerances;
    }

    public void setFieldTolerances(Map<String, BigDecimal> fieldTolerances) {
        this.fieldTolerances = fieldTolerances;
    }

    /**
     * Returns the tolerance for a specific field, checking field-specific overrides first,
     * then falling back to the global numeric tolerance.
     */
    public BigDecimal getToleranceForField(String fieldName) {
        BigDecimal fieldTolerance = fieldTolerances.get(fieldName);
        return fieldTolerance != null ? fieldTolerance : numericTolerance;
    }

    public boolean isRtrimAlphanumeric() {
        return rtrimAlphanumeric;
    }

    public void setRtrimAlphanumeric(boolean rtrimAlphanumeric) {
        this.rtrimAlphanumeric = rtrimAlphanumeric;
    }

    public boolean isIgnoreFiller() {
        return ignoreFiller;
    }

    public void setIgnoreFiller(boolean ignoreFiller) {
        this.ignoreFiller = ignoreFiller;
    }

    public boolean isStripRdw() {
        return stripRdw;
    }

    public void setStripRdw(boolean stripRdw) {
        this.stripRdw = stripRdw;
    }

    public boolean isNormalizeDates() {
        return normalizeDates;
    }

    public void setNormalizeDates(boolean normalizeDates) {
        this.normalizeDates = normalizeDates;
    }

    public SignConvention getSignConvention() {
        return signConvention;
    }

    public void setSignConvention(SignConvention signConvention) {
        this.signConvention = signConvention;
    }

    public Charset getCobolEncoding() {
        return cobolEncoding;
    }

    public void setCobolEncoding(Charset cobolEncoding) {
        this.cobolEncoding = cobolEncoding;
    }

    /**
     * Factory: POSTTRAN profile — exact (ZERO tolerance) for all balance fields.
     */
    public static ToleranceConfig forPosttran() {
        ToleranceConfig config = new ToleranceConfig();
        config.setNumericTolerance(BigDecimal.ZERO);
        return config;
    }

    /**
     * Factory: INTCALC profile — allows 0.01 tolerance on TRAN-AMT because
     * TRAN-CAT-BAL × DIS-INT-RATE / 1200 may differ in rounding between
     * COBOL truncation and Java BigDecimal rounding.
     */
    public static ToleranceConfig forIntcalc() {
        ToleranceConfig config = new ToleranceConfig();
        config.setNumericTolerance(BigDecimal.ZERO);
        config.getFieldTolerances().put("TRAN-AMT", new BigDecimal("0.01"));
        return config;
    }

    /**
     * Factory: TRANSREPT profile — exact for report totals.
     */
    public static ToleranceConfig forTransrept() {
        ToleranceConfig config = new ToleranceConfig();
        config.setNumericTolerance(BigDecimal.ZERO);
        return config;
    }
}
