package com.mainframe.carddemo.migration.job;

import com.mainframe.carddemo.migration.parser.FixedWidthField;
import com.mainframe.carddemo.migration.parser.FixedWidthRecordParser;
import com.mainframe.carddemo.migration.parser.RecordLayouts;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Configuration
public class MigrationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    public MigrationJobConfig(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              DataSource dataSource) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- File Readers ---

    private FlatFileItemReader<String> lineReader(String name, String filePath) {
        return new FlatFileItemReaderBuilder<String>()
                .name(name)
                .resource(new FileSystemResource(filePath))
                .lineMapper((line, lineNumber) -> line)
                .build();
    }

    // --- Account Step ---

    @Bean
    @StepScope
    public FlatFileItemReader<String> accountFileReader(
            @Value("#{jobParameters['dataDir']}/acctdata.txt") String path) {
        return lineReader("accountFileReader", path);
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> accountWriter() {
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .dataSource(dataSource)
                .sql("INSERT INTO account (acct_id, acct_active_status, acct_curr_bal, " +
                     "acct_credit_limit, acct_cash_credit_limit, acct_open_date, " +
                     "acct_expiration_date, acct_reissue_date, acct_curr_cyc_credit, " +
                     "acct_curr_cyc_debit, acct_addr_zip, acct_group_id) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, toLong(item.get("ACCT-ID")));
                    ps.setString(2, toStr(item.get("ACCT-ACTIVE-STATUS")));
                    setBigDecimal(ps, 3, item.get("ACCT-CURR-BAL"));
                    setBigDecimal(ps, 4, item.get("ACCT-CREDIT-LIMIT"));
                    setBigDecimal(ps, 5, item.get("ACCT-CASH-CREDIT-LIMIT"));
                    setDate(ps, 6, item.get("ACCT-OPEN-DATE"));
                    setDate(ps, 7, item.get("ACCT-EXPIRAION-DATE"));
                    setDate(ps, 8, item.get("ACCT-REISSUE-DATE"));
                    setBigDecimal(ps, 9, item.get("ACCT-CURR-CYC-CREDIT"));
                    setBigDecimal(ps, 10, item.get("ACCT-CURR-CYC-DEBIT"));
                    ps.setString(11, toStr(item.get("ACCT-ADDR-ZIP")));
                    ps.setString(12, toStr(item.get("ACCT-GROUP-ID")));
                })
                .build();
    }

    @Bean
    public Step migrateAccountStep(FlatFileItemReader<String> accountFileReader,
                                   JdbcBatchItemWriter<Map<String, Object>> accountWriter) {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.accountLayout());
        return new StepBuilder("migrateAccountStep", jobRepository)
                .<String, Map<String, Object>>chunk(500, transactionManager)
                .reader(accountFileReader)
                .processor((ItemProcessor<String, Map<String, Object>>) line ->
                        line == null || line.trim().isEmpty() ? null : parser.parse(line))
                .writer(accountWriter)
                .build();
    }

    // --- Customer Step ---

    @Bean
    @StepScope
    public FlatFileItemReader<String> customerFileReader(
            @Value("#{jobParameters['dataDir']}/custdata.txt") String path) {
        return lineReader("customerFileReader", path);
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> customerWriter() {
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .dataSource(dataSource)
                .sql("INSERT INTO customer (cust_id, cust_first_name, cust_middle_name, cust_last_name, " +
                     "cust_addr_line_1, cust_addr_line_2, cust_addr_line_3, cust_addr_state_cd, " +
                     "cust_addr_country_cd, cust_addr_zip, cust_phone_num_1, cust_phone_num_2, " +
                     "cust_ssn, cust_govt_issued_id, cust_dob, cust_eft_account_id, " +
                     "cust_pri_card_holder_ind, cust_fico_credit_score) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, toLong(item.get("CUST-ID")));
                    ps.setString(2, toStr(item.get("CUST-FIRST-NAME")));
                    ps.setString(3, toStr(item.get("CUST-MIDDLE-NAME")));
                    ps.setString(4, toStr(item.get("CUST-LAST-NAME")));
                    ps.setString(5, toStr(item.get("CUST-ADDR-LINE-1")));
                    ps.setString(6, toStr(item.get("CUST-ADDR-LINE-2")));
                    ps.setString(7, toStr(item.get("CUST-ADDR-LINE-3")));
                    ps.setString(8, toStr(item.get("CUST-ADDR-STATE-CD")));
                    ps.setString(9, toStr(item.get("CUST-ADDR-COUNTRY-CD")));
                    ps.setString(10, toStr(item.get("CUST-ADDR-ZIP")));
                    ps.setString(11, toStr(item.get("CUST-PHONE-NUM-1")));
                    ps.setString(12, toStr(item.get("CUST-PHONE-NUM-2")));
                    ps.setLong(13, toLong(item.get("CUST-SSN")));
                    ps.setString(14, toStr(item.get("CUST-GOVT-ISSUED-ID")));
                    setDate(ps, 15, item.get("CUST-DOB-YYYY-MM-DD"));
                    ps.setString(16, toStr(item.get("CUST-EFT-ACCOUNT-ID")));
                    ps.setString(17, toStr(item.get("CUST-PRI-CARD-HOLDER-IND")));
                    ps.setInt(18, toInt(item.get("CUST-FICO-CREDIT-SCORE")));
                })
                .build();
    }

    @Bean
    public Step migrateCustomerStep(FlatFileItemReader<String> customerFileReader,
                                    JdbcBatchItemWriter<Map<String, Object>> customerWriter) {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.customerLayout());
        return new StepBuilder("migrateCustomerStep", jobRepository)
                .<String, Map<String, Object>>chunk(500, transactionManager)
                .reader(customerFileReader)
                .processor((ItemProcessor<String, Map<String, Object>>) line ->
                        line == null || line.trim().isEmpty() ? null : parser.parse(line))
                .writer(customerWriter)
                .build();
    }

    // --- Card Step ---

    @Bean
    @StepScope
    public FlatFileItemReader<String> cardFileReader(
            @Value("#{jobParameters['dataDir']}/carddata.txt") String path) {
        return lineReader("cardFileReader", path);
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> cardWriter() {
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .dataSource(dataSource)
                .sql("INSERT INTO card (card_num, card_acct_id, card_cvv_cd, card_embossed_name, " +
                     "card_expiration_date, card_active_status) VALUES (?,?,?,?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setString(1, toStr(item.get("CARD-NUM")));
                    ps.setLong(2, toLong(item.get("CARD-ACCT-ID")));
                    ps.setInt(3, toInt(item.get("CARD-CVV-CD")));
                    ps.setString(4, toStr(item.get("CARD-EMBOSSED-NAME")));
                    setDate(ps, 5, item.get("CARD-EXPIRAION-DATE"));
                    ps.setString(6, toStr(item.get("CARD-ACTIVE-STATUS")));
                })
                .build();
    }

    @Bean
    public Step migrateCardStep(FlatFileItemReader<String> cardFileReader,
                                JdbcBatchItemWriter<Map<String, Object>> cardWriter) {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.cardLayout());
        return new StepBuilder("migrateCardStep", jobRepository)
                .<String, Map<String, Object>>chunk(500, transactionManager)
                .reader(cardFileReader)
                .processor((ItemProcessor<String, Map<String, Object>>) line ->
                        line == null || line.trim().isEmpty() ? null : parser.parse(line))
                .writer(cardWriter)
                .build();
    }

    // --- Card Xref Step ---

    @Bean
    @StepScope
    public FlatFileItemReader<String> cardXrefFileReader(
            @Value("#{jobParameters['dataDir']}/cardxref.txt") String path) {
        return lineReader("cardXrefFileReader", path);
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> cardXrefWriter() {
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .dataSource(dataSource)
                .sql("INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) VALUES (?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setString(1, toStr(item.get("XREF-CARD-NUM")));
                    ps.setLong(2, toLong(item.get("XREF-CUST-ID")));
                    ps.setLong(3, toLong(item.get("XREF-ACCT-ID")));
                })
                .build();
    }

    @Bean
    public Step migrateCardXrefStep(FlatFileItemReader<String> cardXrefFileReader,
                                    JdbcBatchItemWriter<Map<String, Object>> cardXrefWriter) {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.cardXrefLayout());
        return new StepBuilder("migrateCardXrefStep", jobRepository)
                .<String, Map<String, Object>>chunk(500, transactionManager)
                .reader(cardXrefFileReader)
                .processor((ItemProcessor<String, Map<String, Object>>) line ->
                        line == null || line.trim().isEmpty() ? null : parser.parse(line))
                .writer(cardXrefWriter)
                .build();
    }

    // --- Daily Transaction Step ---

    @Bean
    @StepScope
    public FlatFileItemReader<String> dailyTranFileReader(
            @Value("#{jobParameters['dataDir']}/dailytran.txt") String path) {
        return lineReader("dailyTranFileReader", path);
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> dailyTranWriter() {
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .dataSource(dataSource)
                .sql("INSERT INTO daily_transaction (tran_id, tran_type_cd, tran_cat_cd, tran_source, " +
                     "tran_desc, tran_amt, tran_merchant_id, tran_merchant_name, tran_merchant_city, " +
                     "tran_merchant_zip, tran_card_num, tran_orig_ts, tran_proc_ts) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> setTransactionFields(item, ps))
                .build();
    }

    @Bean
    public Step migrateDailyTranStep(FlatFileItemReader<String> dailyTranFileReader,
                                     JdbcBatchItemWriter<Map<String, Object>> dailyTranWriter) {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.transactionLayout());
        return new StepBuilder("migrateDailyTranStep", jobRepository)
                .<String, Map<String, Object>>chunk(500, transactionManager)
                .reader(dailyTranFileReader)
                .processor((ItemProcessor<String, Map<String, Object>>) line ->
                        line == null || line.trim().isEmpty() ? null : parser.parse(line))
                .writer(dailyTranWriter)
                .build();
    }

    // --- Tran Type Step ---

    @Bean
    @StepScope
    public FlatFileItemReader<String> tranTypeFileReader(
            @Value("#{jobParameters['dataDir']}/trantype.txt") String path) {
        return lineReader("tranTypeFileReader", path);
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> tranTypeWriter() {
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .dataSource(dataSource)
                .sql("INSERT INTO tran_type (tran_type, tran_type_desc) VALUES (?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setString(1, toStr(item.get("TRAN-TYPE")));
                    ps.setString(2, toStr(item.get("TRAN-TYPE-DESC")));
                })
                .build();
    }

    @Bean
    public Step migrateTranTypeStep(FlatFileItemReader<String> tranTypeFileReader,
                                    JdbcBatchItemWriter<Map<String, Object>> tranTypeWriter) {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.tranTypeLayout());
        return new StepBuilder("migrateTranTypeStep", jobRepository)
                .<String, Map<String, Object>>chunk(500, transactionManager)
                .reader(tranTypeFileReader)
                .processor((ItemProcessor<String, Map<String, Object>>) line ->
                        line == null || line.trim().isEmpty() ? null : parser.parse(line))
                .writer(tranTypeWriter)
                .build();
    }

    // --- Tran Category Step ---

    @Bean
    @StepScope
    public FlatFileItemReader<String> tranCategoryFileReader(
            @Value("#{jobParameters['dataDir']}/trancatg.txt") String path) {
        return lineReader("tranCategoryFileReader", path);
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> tranCategoryWriter() {
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .dataSource(dataSource)
                .sql("INSERT INTO tran_category (tran_type_cd, tran_cat_cd, tran_cat_type_desc) VALUES (?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setString(1, toStr(item.get("TRAN-TYPE-CD")));
                    ps.setInt(2, toInt(item.get("TRAN-CAT-CD")));
                    ps.setString(3, toStr(item.get("TRAN-CAT-TYPE-DESC")));
                })
                .build();
    }

    @Bean
    public Step migrateTranCategoryStep(FlatFileItemReader<String> tranCategoryFileReader,
                                        JdbcBatchItemWriter<Map<String, Object>> tranCategoryWriter) {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.tranCategoryLayout());
        return new StepBuilder("migrateTranCategoryStep", jobRepository)
                .<String, Map<String, Object>>chunk(500, transactionManager)
                .reader(tranCategoryFileReader)
                .processor((ItemProcessor<String, Map<String, Object>>) line ->
                        line == null || line.trim().isEmpty() ? null : parser.parse(line))
                .writer(tranCategoryWriter)
                .build();
    }

    // --- Tran Cat Balance Step ---

    @Bean
    @StepScope
    public FlatFileItemReader<String> tcatBalFileReader(
            @Value("#{jobParameters['dataDir']}/tcatbal.txt") String path) {
        return lineReader("tcatBalFileReader", path);
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> tcatBalWriter() {
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .dataSource(dataSource)
                .sql("INSERT INTO tran_cat_balance (trancat_acct_id, trancat_type_cd, trancat_cd, tran_cat_bal) " +
                     "VALUES (?,?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setLong(1, toLong(item.get("TRANCAT-ACCT-ID")));
                    ps.setString(2, toStr(item.get("TRANCAT-TYPE-CD")));
                    ps.setInt(3, toInt(item.get("TRANCAT-CD")));
                    setBigDecimal(ps, 4, item.get("TRAN-CAT-BAL"));
                })
                .build();
    }

    @Bean
    public Step migrateTcatBalStep(FlatFileItemReader<String> tcatBalFileReader,
                                   JdbcBatchItemWriter<Map<String, Object>> tcatBalWriter) {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.tranCatBalanceLayout());
        return new StepBuilder("migrateTcatBalStep", jobRepository)
                .<String, Map<String, Object>>chunk(500, transactionManager)
                .reader(tcatBalFileReader)
                .processor((ItemProcessor<String, Map<String, Object>>) line ->
                        line == null || line.trim().isEmpty() ? null : parser.parse(line))
                .writer(tcatBalWriter)
                .build();
    }

    // --- Disclosure Group Step ---

    @Bean
    @StepScope
    public FlatFileItemReader<String> discGroupFileReader(
            @Value("#{jobParameters['dataDir']}/discgrp.txt") String path) {
        return lineReader("discGroupFileReader", path);
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> discGroupWriter() {
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .dataSource(dataSource)
                .sql("INSERT INTO disclosure_group (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd, dis_int_rate) " +
                     "VALUES (?,?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setString(1, toStr(item.get("DIS-ACCT-GROUP-ID")));
                    ps.setString(2, toStr(item.get("DIS-TRAN-TYPE-CD")));
                    ps.setInt(3, toInt(item.get("DIS-TRAN-CAT-CD")));
                    setBigDecimal(ps, 4, item.get("DIS-INT-RATE"));
                })
                .build();
    }

    @Bean
    public Step migrateDiscGroupStep(FlatFileItemReader<String> discGroupFileReader,
                                     JdbcBatchItemWriter<Map<String, Object>> discGroupWriter) {
        FixedWidthRecordParser parser = new FixedWidthRecordParser(RecordLayouts.disclosureGroupLayout());
        return new StepBuilder("migrateDiscGroupStep", jobRepository)
                .<String, Map<String, Object>>chunk(500, transactionManager)
                .reader(discGroupFileReader)
                .processor((ItemProcessor<String, Map<String, Object>>) line ->
                        line == null || line.trim().isEmpty() ? null : parser.parse(line))
                .writer(discGroupWriter)
                .build();
    }

    // --- Migration Job ---

    @Bean
    public Job migrationJob(Step migrateAccountStep,
                            Step migrateCustomerStep,
                            Step migrateCardStep,
                            Step migrateCardXrefStep,
                            Step migrateDailyTranStep,
                            Step migrateTranTypeStep,
                            Step migrateTranCategoryStep,
                            Step migrateTcatBalStep,
                            Step migrateDiscGroupStep) {
        return new JobBuilder("migrationJob", jobRepository)
                .start(migrateTranTypeStep)
                .next(migrateTranCategoryStep)
                .next(migrateAccountStep)
                .next(migrateCustomerStep)
                .next(migrateCardStep)
                .next(migrateCardXrefStep)
                .next(migrateDailyTranStep)
                .next(migrateTcatBalStep)
                .next(migrateDiscGroupStep)
                .build();
    }

    // --- Utility methods ---

    private void setTransactionFields(Map<String, Object> item, PreparedStatement ps)
            throws java.sql.SQLException {
        ps.setString(1, toStr(item.get("TRAN-ID")));
        ps.setString(2, toStr(item.get("TRAN-TYPE-CD")));
        ps.setInt(3, toInt(item.get("TRAN-CAT-CD")));
        ps.setString(4, toStr(item.get("TRAN-SOURCE")));
        ps.setString(5, toStr(item.get("TRAN-DESC")));
        setBigDecimal(ps, 6, item.get("TRAN-AMT"));
        ps.setLong(7, toLong(item.get("TRAN-MERCHANT-ID")));
        ps.setString(8, toStr(item.get("TRAN-MERCHANT-NAME")));
        ps.setString(9, toStr(item.get("TRAN-MERCHANT-CITY")));
        ps.setString(10, toStr(item.get("TRAN-MERCHANT-ZIP")));
        ps.setString(11, toStr(item.get("TRAN-CARD-NUM")));
        setTimestamp(ps, 12, item.get("TRAN-ORIG-TS"));
        setTimestamp(ps, 13, item.get("TRAN-PROC-TS"));
    }

    static String toStr(Object val) {
        return val == null ? "" : val.toString().trim();
    }

    static long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long l) return l;
        if (val instanceof Number n) return n.longValue();
        try { return Long.parseLong(val.toString().trim()); }
        catch (NumberFormatException e) { return 0L; }
    }

    static int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Integer i) return i;
        if (val instanceof Number n) return n.intValue();
        try { return Integer.parseInt(val.toString().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    static void setBigDecimal(PreparedStatement ps, int idx, Object val)
            throws java.sql.SQLException {
        if (val instanceof BigDecimal bd) {
            ps.setBigDecimal(idx, bd);
        } else if (val == null) {
            ps.setBigDecimal(idx, BigDecimal.ZERO);
        } else {
            try { ps.setBigDecimal(idx, new BigDecimal(val.toString().trim())); }
            catch (NumberFormatException e) { ps.setBigDecimal(idx, BigDecimal.ZERO); }
        }
    }

    static void setDate(PreparedStatement ps, int idx, Object val)
            throws java.sql.SQLException {
        if (val == null || val.toString().trim().isEmpty()) {
            ps.setNull(idx, java.sql.Types.DATE);
            return;
        }
        String s = val.toString().trim();
        try {
            ps.setDate(idx, Date.valueOf(LocalDate.parse(s)));
        } catch (DateTimeParseException e) {
            ps.setNull(idx, java.sql.Types.DATE);
        }
    }

    static void setTimestamp(PreparedStatement ps, int idx, Object val)
            throws java.sql.SQLException {
        if (val == null || val.toString().trim().isEmpty()) {
            ps.setNull(idx, java.sql.Types.TIMESTAMP);
            return;
        }
        String s = val.toString().trim();
        try {
            LocalDateTime ldt = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
            ps.setTimestamp(idx, Timestamp.valueOf(ldt));
        } catch (DateTimeParseException e) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                ps.setTimestamp(idx, Timestamp.valueOf(ldt));
            } catch (DateTimeParseException e2) {
                ps.setNull(idx, java.sql.Types.TIMESTAMP);
            }
        }
    }
}
