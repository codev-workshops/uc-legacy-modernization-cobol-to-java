package com.carddemo.harness;

import com.carddemo.harness.edgecase.EdgeCaseHandler;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseHandlerTest {

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
}
