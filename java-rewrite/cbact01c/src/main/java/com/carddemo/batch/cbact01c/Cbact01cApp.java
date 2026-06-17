package com.carddemo.batch.cbact01c;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Main entry point for the CBACT01C batch processor.
 * Reads an account file and writes three output files (OUTFILE, ARRYFILE, VBRCFILE).
 *
 * <p>File paths are specified via system properties or command-line args:
 * <ul>
 *   <li>-Dacctfile=path or arg[0]</li>
 *   <li>-Doutfile=path or arg[1]</li>
 *   <li>-Darryfile=path or arg[2]</li>
 *   <li>-Dvbrcfile=path or arg[3]</li>
 * </ul>
 */
public class Cbact01cApp {

    public static void main(String[] args) {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        String acctFilePath = resolveParam("acctfile", args, 0);
        String outFilePath = resolveParam("outfile", args, 1);
        String arryFilePath = resolveParam("arryfile", args, 2);
        String vbrcFilePath = resolveParam("vbrcfile", args, 3);

        try {
            AccountFileReader reader = new AccountFileReader();
            List<AccountRecord> accounts;
            try (FileReader fr = new FileReader(acctFilePath)) {
                accounts = reader.readAll(fr);
            }

            Cbact01cProcessor processor = new Cbact01cProcessor();
            processor.process(accounts);

            try (FileWriter outWriter = new FileWriter(outFilePath)) {
                new OutFileWriter().writeAll(processor.getOutRecords(), outWriter);
            }

            try (FileWriter arryWriter = new FileWriter(arryFilePath)) {
                new ArrayFileWriter().writeAll(processor.getArrayRecords(), arryWriter);
            }

            try (FileWriter vbrcWriter = new FileWriter(vbrcFilePath)) {
                new VbrcFileWriter().writeAll(processor.getVbrcRecords(), vbrcWriter);
            }

        } catch (IOException e) {
            throw new FileProcessingException("ABENDING PROGRAM", e);
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
    }

    private static String resolveParam(String propName, String[] args, int argIndex) {
        String value = System.getProperty(propName);
        if (value != null && !value.isBlank()) {
            return value;
        }
        if (args != null && args.length > argIndex) {
            return args[argIndex];
        }
        throw new FileProcessingException(
                "Missing required parameter: -D" + propName + "=<path> or positional arg[" + argIndex + "]");
    }
}
