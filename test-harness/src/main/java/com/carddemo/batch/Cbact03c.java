package com.carddemo.batch;

import com.carddemo.batch.model.CardXrefRecord;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * Java translation of CBACT03C.CBL — batch diagnostic utility that reads a
 * VSAM KSDS cross-reference file (CARDXREF) sequentially and DISPLAYs each
 * record twice per read (once inside the read subroutine, once in the main loop).
 */
public class Cbact03c {

    private static final int RECORD_LENGTH = CardXrefRecord.RECORD_LENGTH;
    private static final String PROGRAM_NAME = "CBACT03C";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: Cbact03c <inputFilePath> [outputFilePath]");
            System.exit(1);
        }

        String inputPath = args[0];
        PrintWriter output;
        if (args.length >= 2) {
            try {
                output = new PrintWriter(args[1]);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot open output file: " + args[1]);
                System.exit(1);
                return;
            }
        } else {
            output = new PrintWriter(System.out, true);
        }

        run(inputPath, output);
        output.flush();
        output.close();
    }

    public static void run(String inputFilePath, PrintWriter output) {
        output.println("START OF EXECUTION OF PROGRAM " + PROGRAM_NAME);

        InputStream in;
        try {
            in = new BufferedInputStream(new FileInputStream(inputFilePath));
        } catch (FileNotFoundException e) {
            output.println("ERROR OPENING XREFFILE");
            displayIoStatus(output, e);
            abendProgram(output);
            return;
        }

        try {
            boolean endOfFile = false;

            while (!endOfFile) {
                byte[] buf = new byte[RECORD_LENGTH];
                int totalRead = 0;
                while (totalRead < RECORD_LENGTH) {
                    int bytesRead = in.read(buf, totalRead, RECORD_LENGTH - totalRead);
                    if (bytesRead == -1) {
                        if (totalRead == 0) {
                            endOfFile = true;
                        } else {
                            output.println("ERROR READING XREFFILE");
                            displayIoStatus(output,
                                    new IOException("Incomplete record: expected "
                                            + RECORD_LENGTH + " bytes, got " + totalRead));
                            abendProgram(output);
                            return;
                        }
                        break;
                    }
                    totalRead += bytesRead;
                }

                if (!endOfFile) {
                    String rawRecord = new String(buf, 0, RECORD_LENGTH);
                    CardXrefRecord record = new CardXrefRecord(rawRecord);
                    // DISPLAY inside 1000-XREFFILE-GET-NEXT (line 96)
                    output.println(record.toString());
                    // DISPLAY in main loop (line 78)
                    output.println(record.toString());
                }
            }

            in.close();
        } catch (IOException e) {
            output.println("ERROR READING XREFFILE");
            displayIoStatus(output, e);
            abendProgram(output);
            return;
        }

        output.println("END OF EXECUTION OF PROGRAM " + PROGRAM_NAME);
    }

    private static void displayIoStatus(PrintWriter output, Exception e) {
        String statusCode = "9999";
        output.println("FILE STATUS IS: NNNN" + statusCode);
    }

    private static void abendProgram(PrintWriter output) {
        output.println("ABENDING PROGRAM");
        throw new RuntimeException("ABEND with code 999");
    }
}
