package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.codec.FixedRecordReader;
import com.carddemo.batch.cbact01c.codec.FixedRecordWriter;
import com.carddemo.batch.cbact01c.codec.VariableRecordWriter;
import com.carddemo.batch.cbact01c.codec.ZonedDecimalCodec;
import com.carddemo.batch.cbact01c.date.CobdatftDateFormatter;
import com.carddemo.batch.cbact01c.date.DateFormatter;
import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrArrayRec;
import com.carddemo.batch.cbact01c.model.OutAcctRec;
import com.carddemo.batch.cbact01c.model.Vb1Rec;
import com.carddemo.batch.cbact01c.model.Vb2Rec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/** Java port of the CBACT01C PROCEDURE DIVISION: reads ACCTFILE, writes OUTFILE, ARRYFILE and VBRCFILE. */
public final class Cbact01c {

    public static final String START_MSG = "START OF EXECUTION OF PROGRAM CBACT01C";
    public static final String END_MSG = "END OF EXECUTION OF PROGRAM CBACT01C";

    static final class Options {
        Path acctFile;
        Path outFile;
        Path arryFile;
        Path vbrcFile;
        CobolCharset charset = CobolCharset.EBCDIC;
        boolean display;

        static Options parse(String[] args) {
            Options o = new Options();
            for (int i = 0; i < args.length; i++) {
                String a = args[i];
                boolean hasValue = i + 1 < args.length;
                switch (a) {
                    case "--acctfile" -> o.acctFile = Paths.get(requireValue(a, hasValue, args, i++));
                    case "--outfile" -> o.outFile = Paths.get(requireValue(a, hasValue, args, i++));
                    case "--arryfile" -> o.arryFile = Paths.get(requireValue(a, hasValue, args, i++));
                    case "--vbrcfile" -> o.vbrcFile = Paths.get(requireValue(a, hasValue, args, i++));
                    case "--charset" -> o.charset = CobolCharset.valueOf(
                            requireValue(a, hasValue, args, i++).toUpperCase());
                    case "--display" -> o.display = true;
                    default -> throw new IllegalArgumentException("unknown argument: " + a);
                }
            }
            if (o.acctFile == null || o.outFile == null || o.arryFile == null || o.vbrcFile == null) {
                throw new IllegalArgumentException(
                        "usage: --acctfile <path> --outfile <path> --arryfile <path> --vbrcfile <path>"
                                + " [--charset EBCDIC|ASCII] [--display]");
            }
            return o;
        }

        private static String requireValue(String flag, boolean hasValue, String[] args, int i) {
            if (!hasValue) {
                throw new IllegalArgumentException(flag + " requires a value");
            }
            return args[i + 1];
        }
    }

    private Cbact01c() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out));
    }

    public static int run(String[] args, PrintStream out) {
        Options opts;
        try {
            opts = Options.parse(args);
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
            return IoStatus.ABEND_EXIT_CODE;
        }
        out.println(START_MSG);
        try {
            execute(opts, out);
        } catch (IoStatus e) {
            e.abend(out);
            return IoStatus.ABEND_EXIT_CODE;
        }
        out.println(END_MSG);
        return 0;
    }

    private static void execute(Options opts, PrintStream out) throws IoStatus {
        CobolCharset cs = opts.charset;
        DateFormatter df = new CobdatftDateFormatter();
        try (FixedRecordReader acct = openInput(opts.acctFile, cs);
                FixedRecordWriter outFile = new FixedRecordWriter(
                        openOutput(opts.outFile, "ERROR OPENING OUTFILE"), OutAcctRec.LENGTH);
                FixedRecordWriter arryFile = new FixedRecordWriter(
                        openOutput(opts.arryFile, "ERROR OPENING ARRAYFILE"), ArrArrayRec.LENGTH);
                VariableRecordWriter vbrcFile = new VariableRecordWriter(
                        openOutput(opts.vbrcFile, "ERROR OPENING VBRC FILE"), 84)) {
            BigDecimal carriedDebit = AccountProcessor.INITIAL_CARRIED_DEBIT;
            for (Optional<byte[]> raw = readNext(acct); raw.isPresent(); raw = readNext(acct)) {
                AccountRecord rec = AccountRecord.fromBytes(raw.get(), cs);
                if (opts.display) {
                    display1100(rec, out);
                }
                OutAcctRec outRec = AccountProcessor.populateOut(rec, carriedDebit, df);
                carriedDebit = outRec.currCycDebit();
                write(() -> outFile.write(outRec.toBytes(cs)));
                ArrArrayRec arr = AccountProcessor.populateArr(rec);
                write(() -> arryFile.write(arr.toBytes(cs)));
                Vb1Rec vb1 = AccountProcessor.vb1(rec);
                Vb2Rec vb2 = AccountProcessor.vb2(rec);
                if (opts.display) {
                    out.println("VBRC-REC1:" + asciiText(vb1.toBytes(CobolCharset.ASCII)));
                    out.println("VBRC-REC2:" + asciiText(vb2.toBytes(CobolCharset.ASCII)));
                }
                write(() -> vbrcFile.write(vb1.toBytes(cs)));
                write(() -> vbrcFile.write(vb2.toBytes(cs)));
                if (opts.display) {
                    out.println(cs.decode(raw.get(), 0, raw.get().length));
                }
            }
        } catch (IOException e) {
            throw IoStatus.close(e);
        }
    }

    private static FixedRecordReader openInput(Path file, CobolCharset cs) throws IoStatus {
        try {
            InputStream in = Files.newInputStream(file);
            return new FixedRecordReader(in, AccountRecord.LENGTH, cs);
        } catch (IOException e) {
            throw IoStatus.openInput("ERROR OPENING ACCTFILE", e);
        }
    }

    private static OutputStream openOutput(Path file, String context) throws IoStatus {
        try {
            return Files.newOutputStream(file);
        } catch (IOException e) {
            throw IoStatus.openOutput(context, e);
        }
    }

    private static Optional<byte[]> readNext(FixedRecordReader reader) throws IoStatus {
        try {
            return reader.next();
        } catch (IOException e) {
            throw IoStatus.read(e);
        }
    }

    @FunctionalInterface
    private interface IoAction {
        void run() throws IOException;
    }

    private static void write(IoAction action) throws IoStatus {
        try {
            action.run();
        } catch (IOException e) {
            throw IoStatus.write(e);
        }
    }

    /** 1100-DISPLAY-ACCT-RECORD, rendered in ASCII regardless of the data charset. */
    static void display1100(AccountRecord a, PrintStream out) {
        out.println("ACCT-ID                 :" + a.acctId());
        out.println("ACCT-ACTIVE-STATUS      :" + a.activeStatus());
        out.println("ACCT-CURR-BAL           :" + zoned(a.currBal()));
        out.println("ACCT-CREDIT-LIMIT       :" + zoned(a.creditLimit()));
        out.println("ACCT-CASH-CREDIT-LIMIT  :" + zoned(a.cashCreditLimit()));
        out.println("ACCT-OPEN-DATE          :" + a.openDate());
        out.println("ACCT-EXPIRAION-DATE     :" + a.expirationDate());
        out.println("ACCT-REISSUE-DATE       :" + a.reissueDate());
        out.println("ACCT-CURR-CYC-CREDIT    :" + zoned(a.currCycCredit()));
        out.println("ACCT-CURR-CYC-DEBIT     :" + zoned(a.currCycDebit()));
        out.println("ACCT-GROUP-ID           :" + a.groupId());
        out.println("-------------------------------------------------");
    }

    private static String zoned(BigDecimal value) {
        return asciiText(ZonedDecimalCodec.encodeSigned(value, 10, 2, CobolCharset.ASCII));
    }

    private static String asciiText(byte[] ascii) {
        return new String(ascii, StandardCharsets.US_ASCII);
    }
}
