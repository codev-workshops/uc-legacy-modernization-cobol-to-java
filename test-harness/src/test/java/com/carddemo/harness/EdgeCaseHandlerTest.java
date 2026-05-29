package com.carddemo.harness;

import com.carddemo.harness.edgecase.EdgeCaseHandler;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseHandlerTest {

    // ========== CBACT01C tests ==========

    @Test
    void zeroDebitSubstitutedWith2525() {
        BigDecimal result = EdgeCaseHandler.expectedCycDebit(BigDecimal.ZERO);
        assertEquals(new BigDecimal("2525.00"), result);
    }

    @Test
    void nonZeroDebitReturnedAsIs() {
        BigDecimal input = new BigDecimal("100.00");
        BigDecimal result = EdgeCaseHandler.expectedCycDebit(input);
        assertEquals(input, result);
    }

    @Test
    void negativeDebitReturnedAsIs() {
        BigDecimal input = new BigDecimal("-500.00");
        assertEquals(input, EdgeCaseHandler.expectedCycDebit(input));
    }

    @Test
    void normalizeReissueDateTruncatesTo8Chars() {
        assertEquals("20250520", EdgeCaseHandler.normalizeReissueDate("2025052012"));
    }

    @Test
    void normalizeReissueDateShortString() {
        assertEquals("2025", EdgeCaseHandler.normalizeReissueDate("2025"));
    }

    @Test
    void normalizeReissueDateNull() {
        assertEquals("", EdgeCaseHandler.normalizeReissueDate(null));
    }

    @Test
    void unpopulatedArraySlotIndex4() {
        assertTrue(EdgeCaseHandler.isUnpopulatedArraySlot(4));
    }

    @Test
    void unpopulatedArraySlotIndex5() {
        assertTrue(EdgeCaseHandler.isUnpopulatedArraySlot(5));
    }

    @Test
    void populatedArraySlotIndex1() {
        assertFalse(EdgeCaseHandler.isUnpopulatedArraySlot(1));
    }

    @Test
    void populatedArraySlotIndex3() {
        assertFalse(EdgeCaseHandler.isUnpopulatedArraySlot(3));
    }

    @Test
    void expectedArrayCycDebit1() {
        assertEquals(new BigDecimal("1005.00"), EdgeCaseHandler.expectedArrayCycDebit(1));
    }

    @Test
    void expectedArrayCycDebit2() {
        assertEquals(new BigDecimal("1525.00"), EdgeCaseHandler.expectedArrayCycDebit(2));
    }

    @Test
    void expectedArrayCycDebit3() {
        assertEquals(new BigDecimal("-2500.00"), EdgeCaseHandler.expectedArrayCycDebit(3));
    }

    @Test
    void expectedArrayCycDebit4ReturnsZero() {
        assertEquals(BigDecimal.ZERO, EdgeCaseHandler.expectedArrayCycDebit(4));
    }

    @Test
    void expectedArrayBalance1UsesAccountBalance() {
        BigDecimal acctBal = new BigDecimal("1940.00");
        assertEquals(acctBal, EdgeCaseHandler.expectedArrayBalance(1, acctBal));
    }

    @Test
    void expectedArrayBalance3IsHardcoded() {
        assertEquals(new BigDecimal("-1025.00"),
                EdgeCaseHandler.expectedArrayBalance(3, new BigDecimal("1940.00")));
    }

    @Test
    void expectedArrayBalance4IsZero() {
        assertEquals(BigDecimal.ZERO,
                EdgeCaseHandler.expectedArrayBalance(4, new BigDecimal("1940.00")));
    }

    // ========== CBTRN02C tests ==========

    @Test
    void activeAccountNotRejected() {
        assertFalse(EdgeCaseHandler.isRejectedForInactiveAccount("Y"));
    }

    @Test
    void inactiveAccountRejected() {
        assertTrue(EdgeCaseHandler.isRejectedForInactiveAccount("N"));
    }

    @Test
    void nullStatusRejected() {
        assertTrue(EdgeCaseHandler.isRejectedForInactiveAccount(null));
    }

    @Test
    void cardInXrefNotRejected() {
        Set<String> xrefCards = new HashSet<>();
        xrefCards.add("4111111111111111");
        assertFalse(EdgeCaseHandler.isRejectedForMissingCard("4111111111111111", xrefCards));
    }

    @Test
    void cardNotInXrefRejected() {
        Set<String> xrefCards = new HashSet<>();
        xrefCards.add("4111111111111111");
        assertTrue(EdgeCaseHandler.isRejectedForMissingCard("9999999999999999", xrefCards));
    }

    @Test
    void rejectRecordLengthIs430() {
        assertEquals(430, EdgeCaseHandler.expectedRejectRecordLength());
    }

    // ========== CBACT04C tests ==========

    @Test
    void zeroBalanceSkipsInterest() {
        assertTrue(EdgeCaseHandler.shouldSkipInterestCalc(BigDecimal.ZERO));
    }

    @Test
    void nonZeroBalanceDoesNotSkipInterest() {
        assertFalse(EdgeCaseHandler.shouldSkipInterestCalc(new BigDecimal("1500.00")));
    }

    @Test
    void negativeBalanceDoesNotSkipInterest() {
        assertFalse(EdgeCaseHandler.shouldSkipInterestCalc(new BigDecimal("-100.00")));
    }

    @Test
    void matchingDisclosureGroupNotSkipped() {
        Set<String> discGroupIds = new HashSet<>();
        discGroupIds.add("GRP001");
        assertFalse(EdgeCaseHandler.shouldSkipNoDisclosureGroup("GRP001", discGroupIds));
    }

    @Test
    void missingDisclosureGroupSkipped() {
        Set<String> discGroupIds = new HashSet<>();
        discGroupIds.add("GRP001");
        assertTrue(EdgeCaseHandler.shouldSkipNoDisclosureGroup("GRP999", discGroupIds));
    }

    @Test
    void interestTypeCdIs01() {
        assertEquals("01", EdgeCaseHandler.expectedInterestTypeCd());
    }

    @Test
    void interestCatCdIs0005() {
        assertEquals("0005", EdgeCaseHandler.expectedInterestCatCd());
    }

    // ========== CBTRN03C tests ==========

    @Test
    void pageBoundaryReachedAtLimit() {
        assertTrue(EdgeCaseHandler.isPageBoundary(60, 60));
    }

    @Test
    void pageBoundaryExceeded() {
        assertTrue(EdgeCaseHandler.isPageBoundary(61, 60));
    }

    @Test
    void notAtPageBoundary() {
        assertFalse(EdgeCaseHandler.isPageBoundary(30, 60));
    }

    @Test
    void pageBoundaryAtZero() {
        assertTrue(EdgeCaseHandler.isPageBoundary(0, 0));
    }
}
