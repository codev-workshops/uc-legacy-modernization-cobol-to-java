package com.mainframe.carddemo.report.job.importjob;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class DataImportJobConfig {

    private static final Set<String> VALID_TYPES = Set.of(
            "USR", "CUST", "ACCT", "CARD", "XREF", "TRAN", "DTRAN",
            "TYPE", "CAT", "BAL", "DISC");

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    public DataImportJobConfig(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               DataSource dataSource) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<String> importFileReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        return new FlatFileItemReaderBuilder<String>()
                .name("importFileReader")
                .resource(new FileSystemResource(inputFile))
                .lineMapper((line, lineNumber) -> line)
                .build();
    }

    @Bean
    @StepScope
    public ImportRecordProcessor importRecordProcessor() {
        return new ImportRecordProcessor();
    }

    @Bean
    @StepScope
    public ClassifyingImportWriter classifyingImportWriter(
            @Value("#{jobParameters['schemaPrefix'] ?: 'true'}") String useSchemaPrefix) {
        boolean withSchema = !"false".equalsIgnoreCase(useSchemaPrefix);
        return new ClassifyingImportWriter(new JdbcTemplate(dataSource), withSchema);
    }

    @Bean
    public Step dataImportStep(FlatFileItemReader<String> importFileReader,
                               ImportRecordProcessor importRecordProcessor,
                               ClassifyingImportWriter classifyingImportWriter) {
        return new StepBuilder("dataImportStep", jobRepository)
                .<String, ImportRecord>chunk(500, transactionManager)
                .reader(importFileReader)
                .processor(importRecordProcessor)
                .writer(classifyingImportWriter)
                .build();
    }

    @Bean
    public Job dataImportJob(Step dataImportStep) {
        return new JobBuilder("dataImportJob", jobRepository)
                .start(dataImportStep)
                .build();
    }

    public static class ImportRecordProcessor implements ItemProcessor<String, ImportRecord> {
        @Override
        public ImportRecord process(String line) {
            if (line == null || line.isEmpty()) {
                return null;
            }
            int firstPipe = line.indexOf('|');
            if (firstPipe < 0) {
                return null;
            }
            String type = line.substring(0, firstPipe);
            if (!VALID_TYPES.contains(type)) {
                return null;
            }
            String[] fields = line.substring(firstPipe + 1).split("\\|", -1);
            return new ImportRecord(type, fields);
        }
    }

    public static class ClassifyingImportWriter implements ItemWriter<ImportRecord> {

        private final JdbcTemplate jdbc;
        private final boolean withSchema;
        private final Map<String, String> insertSqls;
        private final ConcurrentHashMap<String, AtomicLong> counts = new ConcurrentHashMap<>();

        public ClassifyingImportWriter(JdbcTemplate jdbc, boolean withSchema) {
            this.jdbc = jdbc;
            this.withSchema = withSchema;
            this.insertSqls = buildInsertSqls(withSchema);
        }

        public Map<String, Long> getCounts() {
            Map<String, Long> result = new HashMap<>();
            counts.forEach((k, v) -> result.put(k, v.get()));
            return result;
        }

        @Override
        public void write(Chunk<? extends ImportRecord> chunk) {
            for (ImportRecord rec : chunk) {
                String sql = insertSqls.get(rec.getType());
                if (sql == null) continue;
                try {
                    jdbc.update(sql, (Object[]) rec.getFields());
                    counts.computeIfAbsent(rec.getType(), k -> new AtomicLong()).incrementAndGet();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to import " + rec.getType() + " record", e);
                }
            }
        }

        private static Map<String, String> buildInsertSqls(boolean withSchema) {
            Map<String, String> sqls = new HashMap<>();
            sqls.put("USR", insert(withSchema, "security", "user_security",
                    "usr_id", "usr_fname", "usr_lname", "usr_pwd", "usr_type"));
            sqls.put("CUST", insert(withSchema, "account", "customer",
                    "cust_id", "cust_first_name", "cust_middle_name", "cust_last_name",
                    "cust_addr_line_1", "cust_addr_line_2", "cust_addr_line_3",
                    "cust_addr_state_cd", "cust_addr_country_cd", "cust_addr_zip",
                    "cust_phone_num_1", "cust_phone_num_2", "cust_ssn",
                    "cust_govt_issued_id", "cust_dob", "cust_eft_account_id",
                    "cust_pri_card_holder_ind", "cust_fico_credit_score"));
            sqls.put("ACCT", insert(withSchema, "account", "account",
                    "acct_id", "acct_active_status", "acct_curr_bal", "acct_credit_limit",
                    "acct_cash_credit_limit", "acct_open_date", "acct_expiration_date",
                    "acct_reissue_date", "acct_curr_cyc_credit", "acct_curr_cyc_debit",
                    "acct_addr_zip", "acct_group_id"));
            sqls.put("CARD", insert(withSchema, "account", "card",
                    "card_num", "card_acct_id", "card_cvv_cd", "card_embossed_name",
                    "card_expiration_date", "card_active_status"));
            sqls.put("XREF", insert(withSchema, "account", "card_xref",
                    "xref_card_num", "xref_cust_id", "xref_acct_id"));
            sqls.put("TRAN", insert(withSchema, "transaction", "transaction",
                    "tran_id", "tran_type_cd", "tran_cat_cd", "tran_source", "tran_desc",
                    "tran_amt", "tran_merchant_id", "tran_merchant_name",
                    "tran_merchant_city", "tran_merchant_zip", "tran_card_num",
                    "tran_orig_ts", "tran_proc_ts"));
            sqls.put("DTRAN", insert(withSchema, "transaction", "daily_transaction",
                    "tran_id", "tran_type_cd", "tran_cat_cd", "tran_source", "tran_desc",
                    "tran_amt", "tran_merchant_id", "tran_merchant_name",
                    "tran_merchant_city", "tran_merchant_zip", "tran_card_num",
                    "tran_orig_ts", "tran_proc_ts"));
            sqls.put("TYPE", insert(withSchema, "transaction", "tran_type",
                    "tran_type", "tran_type_desc"));
            sqls.put("CAT", insert(withSchema, "transaction", "tran_category",
                    "tran_type_cd", "tran_cat_cd", "tran_cat_type_desc"));
            sqls.put("BAL", insert(withSchema, "transaction", "tran_cat_balance",
                    "trancat_acct_id", "trancat_type_cd", "trancat_cd", "tran_cat_bal"));
            sqls.put("DISC", insert(withSchema, "transaction", "disclosure_group",
                    "dis_acct_group_id", "dis_tran_type_cd", "dis_tran_cat_cd", "dis_int_rate"));
            return sqls;
        }

        private static String insert(boolean withSchema, String schema, String table, String... cols) {
            String qualifiedTable = withSchema ? schema + "." + table : table;
            String placeholders = String.join(", ", java.util.Collections.nCopies(cols.length, "?"));
            return "INSERT INTO " + qualifiedTable + " (" + String.join(", ", cols) + ") VALUES (" + placeholders + ")";
        }
    }
}
