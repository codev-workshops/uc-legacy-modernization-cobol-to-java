package com.carddemo.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaitUtilTest {

    @Test
    void waitCentisecondsPositive() throws InterruptedException {
        long start = System.currentTimeMillis();
        WaitUtil.waitCentiseconds(5); // 50ms
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 40, "Expected at least ~50ms sleep, got " + elapsed);
    }

    @Test
    void waitCentisecondsZero() throws InterruptedException {
        long start = System.currentTimeMillis();
        WaitUtil.waitCentiseconds(0);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 50, "Zero wait should return immediately");
    }

    @Test
    void waitCentisecondsNegative() throws InterruptedException {
        long start = System.currentTimeMillis();
        WaitUtil.waitCentiseconds(-1);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 50, "Negative wait should return immediately");
    }

    @Test
    void waitMillisPositive() throws InterruptedException {
        long start = System.currentTimeMillis();
        WaitUtil.waitMillis(50);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 40, "Expected at least ~50ms sleep, got " + elapsed);
    }

    @Test
    void waitMillisZero() throws InterruptedException {
        long start = System.currentTimeMillis();
        WaitUtil.waitMillis(0);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 50, "Zero wait should return immediately");
    }

    @Test
    void waitMillisNegative() throws InterruptedException {
        long start = System.currentTimeMillis();
        WaitUtil.waitMillis(-100);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 50, "Negative wait should return immediately");
    }
}
