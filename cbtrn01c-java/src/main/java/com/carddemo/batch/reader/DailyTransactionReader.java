package com.carddemo.batch.reader;

import com.carddemo.batch.model.DailyTransaction;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.core.io.PathResource;

import java.nio.file.Path;

/**
 * Spring Batch reader for sequential daily transaction file (DALYTRAN).
 * Replaces COBOL paragraph 1000-DALYTRAN-GET-NEXT (lines 202-225 of CBTRN01C.cbl).
 * Reads 350-byte fixed-width records and maps via DailyTransaction.fromFixedLength().
 */
public class DailyTransactionReader extends FlatFileItemReader<DailyTransaction> {

    public DailyTransactionReader(Path dalytranFilePath) {
        setName("dailyTransactionReader");
        setResource(new PathResource(dalytranFilePath));
        setLineMapper(new FixedLengthTransactionLineMapper());
    }

    private static class FixedLengthTransactionLineMapper implements LineMapper<DailyTransaction> {
        @Override
        public DailyTransaction mapLine(String line, int lineNumber) {
            return DailyTransaction.fromFixedLength(line);
        }
    }
}
