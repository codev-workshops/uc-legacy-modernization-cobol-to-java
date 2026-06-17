package com.carddemo.batch.cbact01c;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a flat file (pipe-delimited or fixed-width 300-byte records) and produces
 * AccountRecord objects. Also supports constructing records programmatically for testing.
 *
 * <p>Pipe-delimited format (one record per line):
 * acctId|activeStatus|currBal|creditLimit|cashCreditLimit|openDate|expirationDate|reissueDate|currCycCredit|currCycDebit|addrZip|groupId
 */
public class AccountFileReader {

    /**
     * Parse account records from a Reader (pipe-delimited format).
     *
     * @param reader source of account data
     * @return list of parsed AccountRecord objects
     * @throws FileProcessingException on I/O errors
     */
    public List<AccountRecord> readAll(Reader reader) {
        List<AccountRecord> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                records.add(parseLine(line));
            }
        } catch (IOException e) {
            throw new FileProcessingException("ERROR READING ACCOUNT FILE", e);
        }
        return records;
    }

    private AccountRecord parseLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 12) {
            throw new FileProcessingException(
                    "Invalid record format: expected 12 pipe-delimited fields, got " + parts.length);
        }
        return new AccountRecord(
                Long.parseLong(parts[0].trim()),
                parts[1].trim(),
                new BigDecimal(parts[2].trim()),
                new BigDecimal(parts[3].trim()),
                new BigDecimal(parts[4].trim()),
                parts[5].trim(),
                parts[6].trim(),
                parts[7].trim(),
                new BigDecimal(parts[8].trim()),
                new BigDecimal(parts[9].trim()),
                parts[10].trim(),
                parts[11].trim()
        );
    }
}
