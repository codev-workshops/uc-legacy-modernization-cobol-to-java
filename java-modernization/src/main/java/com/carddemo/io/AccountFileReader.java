package com.carddemo.io;

import com.carddemo.model.AccountRecord;
import com.carddemo.util.CobolDecimalParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Reads fixed-length (300-byte) account records from an ASCII data file.
 * Equivalent to the COBOL indexed-file sequential read of ACCTFILE.
 */
public class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;

    public AccountFileReader(Path path) throws IOException {
        this.reader = Files.newBufferedReader(path);
    }

    public Optional<AccountRecord> readNext() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return Optional.empty();
        }
        // Pad to full record length if shorter
        line = String.format("%-" + AccountRecord.RECORD_LENGTH + "s", line);
        return Optional.of(parseLine(line));
    }

    private AccountRecord parseLine(String line) {
        int pos = 0;

        String acctId = line.substring(pos, pos + 11);
        pos += 11;

        String activeStatus = line.substring(pos, pos + 1);
        pos += 1;

        BigDecimal currBal = CobolDecimalParser.parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        BigDecimal creditLimit = CobolDecimalParser.parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        BigDecimal cashCreditLimit = CobolDecimalParser.parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        String openDate = line.substring(pos, pos + 10);
        pos += 10;

        String expirationDate = line.substring(pos, pos + 10);
        pos += 10;

        String reissueDate = line.substring(pos, pos + 10);
        pos += 10;

        BigDecimal currCycCredit = CobolDecimalParser.parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        BigDecimal currCycDebit = CobolDecimalParser.parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        String addrZip = line.substring(pos, pos + 10);
        pos += 10;

        String groupId = line.substring(pos, pos + 10);

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
