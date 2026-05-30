package com.carddemo.common.util;

/**
 * Replaces the MVSWAIT assembler routine called at COBSWAIT.cbl line 38.
 * The COBOL program passes a PIC 9(8) COMP value representing centiseconds.
 */
public final class WaitUtil {

    private WaitUtil() {}

    /**
     * Sleeps for the specified number of centiseconds (1/100th of a second).
     *
     * @param centiseconds the wait time in centiseconds
     * @throws InterruptedException if the thread is interrupted
     */
    public static void waitCentiseconds(long centiseconds) throws InterruptedException {
        if (centiseconds <= 0) {
            return;
        }
        Thread.sleep(centiseconds * 10);
    }

    /**
     * Sleeps for the specified number of milliseconds.
     *
     * @param millis the wait time in milliseconds
     * @throws InterruptedException if the thread is interrupted
     */
    public static void waitMillis(long millis) throws InterruptedException {
        if (millis <= 0) {
            return;
        }
        Thread.sleep(millis);
    }
}
