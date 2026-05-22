package com.carddemo.harness.config;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
}
