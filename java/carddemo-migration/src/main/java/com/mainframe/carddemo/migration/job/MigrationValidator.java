package com.mainframe.carddemo.migration.job;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class MigrationValidator {

    private final JdbcTemplate jdbcTemplate;

    public MigrationValidator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, ValidationEntry> validate(String dataDir) {
        Map<String, ValidationEntry> results = new LinkedHashMap<>();
        results.put("account", check("account", dataDir, "acctdata.txt"));
        results.put("customer", check("customer", dataDir, "custdata.txt"));
        results.put("card", check("card", dataDir, "carddata.txt"));
        results.put("card_xref", check("card_xref", dataDir, "cardxref.txt"));
        results.put("daily_transaction", check("daily_transaction", dataDir, "dailytran.txt"));
        results.put("tran_type", check("tran_type", dataDir, "trantype.txt"));
        results.put("tran_category", check("tran_category", dataDir, "trancatg.txt"));
        results.put("tran_cat_balance", check("tran_cat_balance", dataDir, "tcatbal.txt"));
        results.put("disclosure_group", check("disclosure_group", dataDir, "discgrp.txt"));
        return results;
    }

    private ValidationEntry check(String table, String dataDir, String filename) {
        long fileLines = countFileLines(Path.of(dataDir, filename));
        long dbRows = countTableRows(table);
        boolean passed = fileLines == dbRows;
        return new ValidationEntry(table, fileLines, dbRows, passed);
    }

    long countFileLines(Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            long count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    count++;
                }
            }
            return count;
        } catch (IOException e) {
            return -1;
        }
    }

    long countTableRows(String table) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
            return count == null ? 0 : count;
        } catch (Exception e) {
            return -1;
        }
    }

    public record ValidationEntry(String table, long expectedCount, long actualCount, boolean passed) {}
}
