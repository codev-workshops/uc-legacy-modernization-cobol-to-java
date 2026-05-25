package com.carddemo.batch;

import com.carddemo.batch.service.AccountFileProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the CBACT01C Java batch application.
 *
 * Usage: java -jar cbact01c-java.jar <inputFile> <outputFile> <arrayFile> <vbrcFile>
 *
 * Equivalent to running the COBOL CBACT01C program with DD assignments:
 *   ACCTFILE  → inputFile  (indexed account file, read sequentially)
 *   OUTFILE   → outputFile (sequential account summary)
 *   ARRYFILE  → arrayFile  (sequential array records)
 *   VBRCFILE  → vbrcFile   (sequential variable-length records)
 */
public class Cbact01cApplication {

    private static final Logger LOG = Logger.getLogger(Cbact01cApplication.class.getName());
    private static final int ABEND_CODE = 999;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: CBACT01C <inputFile> <outputFile> <arrayFile> <vbrcFile>");
            System.exit(1);
        }

        Path inputFile = Path.of(args[0]);
        Path outputFile = Path.of(args[1]);
        Path arrayFile = Path.of(args[2]);
        Path vbrcFile = Path.of(args[3]);

        AccountFileProcessor processor = new AccountFileProcessor(
                inputFile, outputFile, arrayFile, vbrcFile);

        try {
            int count = processor.process();
            LOG.info("Processing complete. Records processed: " + count);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "ABENDING PROGRAM - I/O error", e);
            System.exit(ABEND_CODE);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "ABENDING PROGRAM - Unexpected error", e);
            System.exit(ABEND_CODE);
        }
    }
}
