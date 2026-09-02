package com.carddemo.batch.cbact01c;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;

/**
 * COBOL file-status modelling for CBACT01C: maps Java I/O failures to the two-character
 * status codes the program tests, and reproduces 9910-DISPLAY-IO-STATUS / 9999-ABEND-PROGRAM.
 */
public final class IoStatus extends Exception {

    private static final long serialVersionUID = 1L;

    public static final String OK = "00";
    public static final String EOF = "10";
    public static final String SHORT_RECORD = "04";
    public static final String IO_ERROR = "30";
    public static final String NOT_FOUND = "35";

    /** Exit code used in place of the U0999 abend (APPL-RESULT error value). */
    public static final int ABEND_EXIT_CODE = 12;

    private final String contextLine;
    private final String status;

    public IoStatus(String contextLine, String status, Throwable cause) {
        super(contextLine + " (" + status + ")", cause);
        this.contextLine = contextLine;
        this.status = status;
    }

    public String contextLine() {
        return contextLine;
    }

    public String status() {
        return status;
    }

    /** OPEN INPUT failure: missing file is '35', anything else '30'. */
    public static IoStatus openInput(String contextLine, IOException cause) {
        return new IoStatus(contextLine, cause instanceof NoSuchFileException ? NOT_FOUND : IO_ERROR, cause);
    }

    /** OPEN OUTPUT failure ('30'); the COBOL DISPLAY appends the status to the context line. */
    public static IoStatus openOutput(String contextLine, IOException cause) {
        return new IoStatus(contextLine + IO_ERROR, IO_ERROR, cause);
    }

    /** READ failure: a short final record is '04', anything else '30'. */
    public static IoStatus read(IOException cause) {
        return new IoStatus("ERROR READING ACCOUNT FILE", cause instanceof EOFException ? SHORT_RECORD : IO_ERROR,
                cause);
    }

    /** WRITE failure on any of the three output files. */
    public static IoStatus write(IOException cause) {
        return new IoStatus("ACCOUNT FILE WRITE STATUS IS:" + IO_ERROR, IO_ERROR, cause);
    }

    /** CLOSE failure on the account file. */
    public static IoStatus close(IOException cause) {
        return new IoStatus("ERROR CLOSING ACCOUNT FILE", IO_ERROR, cause);
    }

    /** 9910-DISPLAY-IO-STATUS: IO-STATUS-04 rendering of a two-character status. */
    public static String displayIoStatus(String status) {
        char stat1 = status.charAt(0);
        char stat2 = status.charAt(1);
        boolean numeric = Character.isDigit(stat1) && Character.isDigit(stat2);
        if (!numeric || stat1 == '9') {
            return "FILE STATUS IS: NNNN" + stat1 + String.format("%03d", (int) stat2);
        }
        return "FILE STATUS IS: NNNN" + "00" + status;
    }

    /** Context DISPLAY + 9910-DISPLAY-IO-STATUS + 9999-ABEND-PROGRAM. */
    public void abend(PrintStream out) {
        out.println(contextLine);
        out.println(displayIoStatus(status));
        out.println("ABENDING PROGRAM");
    }
}
