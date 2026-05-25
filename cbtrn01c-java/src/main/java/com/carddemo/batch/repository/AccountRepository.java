package com.carddemo.batch.repository;

import com.carddemo.batch.model.AccountRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simulates COBOL indexed-file random access (READ ACCOUNT-FILE KEY IS FD-ACCT-ID)
 * by pre-loading all account records into a Map keyed by account ID.
 * Equivalent to COBOL paragraph 3000-READ-ACCOUNT (lines 241-250 of CBTRN01C.cbl).
 */
public class AccountRepository {

    private final Map<Long, AccountRecord> accountMap = new HashMap<>();

    public AccountRepository(Path acctFilePath) throws IOException {
        loadRecords(acctFilePath);
    }

    private void loadRecords(Path filePath) throws IOException {
        for (String line : Files.readAllLines(filePath)) {
            if (line.length() >= 122) {
                AccountRecord record = AccountRecord.fromFixedLength(line);
                accountMap.put(record.acctId(), record);
            }
        }
    }

    /**
     * Looks up an account record by account ID.
     * Equivalent to COBOL's READ ACCOUNT-FILE KEY IS FD-ACCT-ID.
     */
    public Optional<AccountRecord> findByAcctId(long acctId) {
        return Optional.ofNullable(accountMap.get(acctId));
    }

    public int size() {
        return accountMap.size();
    }
}
