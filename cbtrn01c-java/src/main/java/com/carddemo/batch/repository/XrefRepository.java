package com.carddemo.batch.repository;

import com.carddemo.batch.model.CardXrefRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simulates COBOL indexed-file random access (READ XREF-FILE KEY IS FD-XREF-CARD-NUM)
 * by pre-loading all XREF records into a Map keyed by card number.
 * Equivalent to COBOL paragraph 2000-LOOKUP-XREF (lines 227-239 of CBTRN01C.cbl).
 */
public class XrefRepository {

    private final Map<String, CardXrefRecord> xrefMap = new HashMap<>();

    public XrefRepository(Path xrefFilePath) throws IOException {
        loadRecords(xrefFilePath);
    }

    private void loadRecords(Path filePath) throws IOException {
        for (String line : Files.readAllLines(filePath)) {
            if (line.length() >= 36) {
                CardXrefRecord record = CardXrefRecord.fromFixedLength(line);
                xrefMap.put(record.cardNum(), record);
            }
        }
    }

    /**
     * Looks up a card cross-reference record by card number.
     * Equivalent to COBOL's READ XREF-FILE KEY IS FD-XREF-CARD-NUM.
     */
    public Optional<CardXrefRecord> findByCardNum(String cardNum) {
        return Optional.ofNullable(xrefMap.get(cardNum));
    }

    public int size() {
        return xrefMap.size();
    }
}
