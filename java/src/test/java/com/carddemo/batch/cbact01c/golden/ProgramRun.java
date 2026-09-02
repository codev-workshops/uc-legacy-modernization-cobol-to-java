package com.carddemo.batch.cbact01c.golden;

import com.carddemo.batch.cbact01c.Cbact01c;
import com.carddemo.batch.cbact01c.codec.CobolCharset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Test support: runs CBACT01C into a directory and reads back its three output datasets. */
public final class ProgramRun {

    public static final int VB_RDW_LENGTH = 4;

    private final int exitCode;
    private final String console;
    private final byte[] outfile;
    private final byte[] arryfile;
    private final byte[] vbrcfile;

    private ProgramRun(int exitCode, String console, byte[] outfile, byte[] arryfile, byte[] vbrcfile) {
        this.exitCode = exitCode;
        this.console = console;
        this.outfile = outfile;
        this.arryfile = arryfile;
        this.vbrcfile = vbrcfile;
    }

    public static ProgramRun execute(Path acctFile, Path dir, CobolCharset cs) throws IOException {
        Files.createDirectories(dir);
        Path outfile = dir.resolve("OUTFILE");
        Path arryfile = dir.resolve("ARRYFILE");
        Path vbrcfile = dir.resolve("VBRCFILE");
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        int rc = Cbact01c.run(
                new String[] {
                    "--acctfile", acctFile.toString(),
                    "--outfile", outfile.toString(),
                    "--arryfile", arryfile.toString(),
                    "--vbrcfile", vbrcfile.toString(),
                    "--charset", cs.name()},
                new PrintStream(console, true, StandardCharsets.UTF_8));
        return new ProgramRun(rc, console.toString(StandardCharsets.UTF_8), Files.readAllBytes(outfile),
                Files.readAllBytes(arryfile), Files.readAllBytes(vbrcfile));
    }

    public int exitCode() {
        return exitCode;
    }

    public String console() {
        return console;
    }

    public byte[] outfile() {
        return outfile;
    }

    public byte[] arryfile() {
        return arryfile;
    }

    public byte[] vbrcfile() {
        return vbrcfile;
    }

    /** Splits a RECFM=FB dataset into its fixed-length records. */
    public static List<byte[]> fixedRecords(byte[] dataset, int lrecl) {
        List<byte[]> records = new ArrayList<>(dataset.length / lrecl);
        for (int off = 0; off + lrecl <= dataset.length; off += lrecl) {
            records.add(Arrays.copyOfRange(dataset, off, off + lrecl));
        }
        return records;
    }

    /** Splits a RECFM=VB dataset into its logical records, dropping the 4-byte RDWs. */
    public static List<byte[]> variableRecords(byte[] dataset) {
        List<byte[]> records = new ArrayList<>();
        int off = 0;
        while (off < dataset.length) {
            int rdw = ((dataset[off] & 0xFF) << 8) | (dataset[off + 1] & 0xFF);
            int dataLength = rdw - VB_RDW_LENGTH;
            records.add(Arrays.copyOfRange(dataset, off + VB_RDW_LENGTH, off + VB_RDW_LENGTH + dataLength));
            off += rdw;
        }
        return records;
    }

    /** Loads a fixture from {@code src/test/resources/golden}, independent of the working directory. */
    public static byte[] fixture(String name) {
        try (InputStream in = ProgramRun.class.getResourceAsStream("/golden/" + name)) {
            if (in == null) {
                throw new IllegalStateException("missing test fixture golden/" + name);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Writes a fixture to {@code dir} so the program can be pointed at a real file. */
    public static Path fixtureFile(String name, Path dir) throws IOException {
        Path file = dir.resolve(name);
        Files.write(file, fixture(name));
        return file;
    }
}
