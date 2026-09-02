package com.carddemo.batch.cbact01c.model;

import com.carddemo.batch.cbact01c.codec.CobolCharset;
import com.carddemo.batch.cbact01c.codec.FixedRecordReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Test helper: locates and reads the 50-record sample files under app/data. */
public final class SampleData {

    private SampleData() {
    }

    public static Path repoRoot() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null && !Files.isDirectory(dir.resolve("app/data"))) {
            dir = dir.getParent();
        }
        if (dir == null) {
            throw new IllegalStateException("app/data not found above " + Paths.get("").toAbsolutePath());
        }
        return dir;
    }

    public static Path ebcdicAcctData() {
        return repoRoot().resolve("app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS");
    }

    public static Path asciiAcctData() {
        return repoRoot().resolve("app/data/ASCII/acctdata.txt");
    }

    public static List<byte[]> readAll(Path file, CobolCharset cs) throws IOException {
        List<byte[]> records = new ArrayList<>();
        try (InputStream in = Files.newInputStream(file);
                FixedRecordReader reader = new FixedRecordReader(in, AccountRecord.LENGTH, cs)) {
            for (Optional<byte[]> rec = reader.next(); rec.isPresent(); rec = reader.next()) {
                records.add(rec.get());
            }
        }
        return records;
    }
}
