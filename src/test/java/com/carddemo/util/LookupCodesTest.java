package com.carddemo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LookupCodesTest {

    @Test
    void phoneAreaCodeSetSize() {
        assertEquals(490, LookupCodes.VALID_PHONE_AREA_CODES.size());
    }

    @Test
    void stateCodeSetSize() {
        assertEquals(56, LookupCodes.VALID_US_STATE_CODES.size());
    }

    @Test
    void stateZipPrefixSetSize() {
        assertEquals(240, LookupCodes.VALID_US_STATE_ZIP_PREFIXES.size());
    }

    @Test
    void knownValidPhoneAreaCodes() {
        assertTrue(LookupCodes.isValidPhoneAreaCode("212"));
        assertTrue(LookupCodes.isValidPhoneAreaCode("310"));
        assertTrue(LookupCodes.isValidPhoneAreaCode("415"));
        assertTrue(LookupCodes.isValidPhoneAreaCode("800"));
        assertTrue(LookupCodes.isValidPhoneAreaCode("900"));
    }

    @Test
    void invalidPhoneAreaCodes() {
        assertFalse(LookupCodes.isValidPhoneAreaCode("000"));
        assertFalse(LookupCodes.isValidPhoneAreaCode("123"));
        assertFalse(LookupCodes.isValidPhoneAreaCode("XYZ"));
    }

    @Test
    void phoneAreaCodeTrimsWhitespace() {
        assertTrue(LookupCodes.isValidPhoneAreaCode(" 212 "));
        assertTrue(LookupCodes.isValidPhoneAreaCode("212 "));
    }

    @Test
    void phoneAreaCodeNullReturnsfalse() {
        assertFalse(LookupCodes.isValidPhoneAreaCode(null));
    }

    @Test
    void phoneAreaCodeEmptyReturnsFalse() {
        assertFalse(LookupCodes.isValidPhoneAreaCode(""));
    }

    @Test
    void knownValidStateCodes() {
        assertTrue(LookupCodes.isValidStateCode("NY"));
        assertTrue(LookupCodes.isValidStateCode("CA"));
        assertTrue(LookupCodes.isValidStateCode("TX"));
        assertTrue(LookupCodes.isValidStateCode("DC"));
        assertTrue(LookupCodes.isValidStateCode("PR"));
        assertTrue(LookupCodes.isValidStateCode("GU"));
        assertTrue(LookupCodes.isValidStateCode("VI"));
    }

    @Test
    void stateCodeCaseInsensitive() {
        assertTrue(LookupCodes.isValidStateCode("ny"));
        assertTrue(LookupCodes.isValidStateCode("ca"));
        assertTrue(LookupCodes.isValidStateCode("Tx"));
    }

    @Test
    void invalidStateCodes() {
        assertFalse(LookupCodes.isValidStateCode("XX"));
        assertFalse(LookupCodes.isValidStateCode("ZZ"));
    }

    @Test
    void stateCodeNullReturnsFalse() {
        assertFalse(LookupCodes.isValidStateCode(null));
    }

    @Test
    void stateCodeEmptyReturnsFalse() {
        assertFalse(LookupCodes.isValidStateCode(""));
    }

    @Test
    void stateCodeTrimsWhitespace() {
        assertTrue(LookupCodes.isValidStateCode(" NY "));
    }

    @Test
    void knownValidStateZipPrefixes() {
        assertTrue(LookupCodes.isValidStateZipPrefix("NY", "10"));
        assertTrue(LookupCodes.isValidStateZipPrefix("CA", "90"));
        assertTrue(LookupCodes.isValidStateZipPrefix("TX", "75"));
        assertTrue(LookupCodes.isValidStateZipPrefix("DC", "20"));
    }

    @Test
    void stateZipPrefixWithFullZip() {
        assertTrue(LookupCodes.isValidStateZipPrefix("NY", "10001"));
    }

    @Test
    void invalidStateZipPrefix() {
        assertFalse(LookupCodes.isValidStateZipPrefix("NY", "99"));
        assertFalse(LookupCodes.isValidStateZipPrefix("XX", "10"));
    }

    @Test
    void stateZipPrefixNullStateReturnsFalse() {
        assertFalse(LookupCodes.isValidStateZipPrefix(null, "10"));
    }

    @Test
    void stateZipPrefixNullZipReturnsFalse() {
        assertFalse(LookupCodes.isValidStateZipPrefix("NY", null));
    }

    @Test
    void stateZipPrefixBothNullReturnsFalse() {
        assertFalse(LookupCodes.isValidStateZipPrefix(null, null));
    }

    @Test
    void stateZipPrefixTrimsAndUppercases() {
        assertTrue(LookupCodes.isValidStateZipPrefix(" ny ", " 10 "));
    }

    @Test
    void setsAreImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> LookupCodes.VALID_PHONE_AREA_CODES.add("000"));
        assertThrows(UnsupportedOperationException.class,
                () -> LookupCodes.VALID_US_STATE_CODES.add("XX"));
        assertThrows(UnsupportedOperationException.class,
                () -> LookupCodes.VALID_US_STATE_ZIP_PREFIXES.add("XX00"));
    }
}
