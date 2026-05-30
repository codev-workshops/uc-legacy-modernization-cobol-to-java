package com.mainframe.carddemo.report.job.export;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataExportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    public DataExportJobConfig(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               DataSource dataSource) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
    }

    @Bean
    @StepScope
    public CompositeItemReader<String> exportCompositeReader(
            @Value("#{jobParameters['schemaPrefix'] ?: 'true'}") String useSchemaPrefix) {
        boolean withSchema = !"false".equalsIgnoreCase(useSchemaPrefix);
        List<ItemStreamReader<String>> readers = new ArrayList<>();
        readers.add(tableReader("usrReader", "USR",
                selectFrom(withSchema, "security", "user_security",
                        "usr_id", "usr_fname", "usr_lname", "usr_pwd", "usr_type")));
        readers.add(tableReader("custReader", "CUST",
                selectFrom(withSchema, "account", "customer",
                        "cust_id", "cust_first_name", "cust_middle_name", "cust_last_name",
                        "cust_addr_line_1", "cust_addr_line_2", "cust_addr_line_3",
                        "cust_addr_state_cd", "cust_addr_country_cd", "cust_addr_zip",
                        "cust_phone_num_1", "cust_phone_num_2", "cust_ssn",
                        "cust_govt_issued_id", "cust_dob", "cust_eft_account_id",
                        "cust_pri_card_holder_ind", "cust_fico_credit_score")));
        readers.add(tableReader("acctReader", "ACCT",
                selectFrom(withSchema, "account", "account",
                        "acct_id", "acct_active_status", "acct_curr_bal", "acct_credit_limit",
                        "acct_cash_credit_limit", "acct_open_date", "acct_expiration_date",
                        "acct_reissue_date", "acct_curr_cyc_credit", "acct_curr_cyc_debit",
                        "acct_addr_zip", "acct_group_id")));
        readers.add(tableReader("cardReader", "CARD",
                selectFrom(withSchema, "account", "card",
                        "card_num", "card_acct_id", "card_cvv_cd", "card_embossed_name",
                        "card_expiration_date", "card_active_status")));
        readers.add(tableReader("xrefReader", "XREF",
                selectFrom(withSchema, "account", "card_xref",
                        "xref_card_num", "xref_cust_id", "xref_acct_id")));
        readers.add(tableReader("tranReader", "TRAN",
                selectFrom(withSchema, "transaction", "transaction",
                        "tran_id", "tran_type_cd", "tran_cat_cd", "tran_source", "tran_desc",
                        "tran_amt", "tran_merchant_id", "tran_merchant_name",
                        "tran_merchant_city", "tran_merchant_zip", "tran_card_num",
                        "tran_orig_ts", "tran_proc_ts")));
        readers.add(tableReader("dtranReader", "DTRAN",
                selectFrom(withSchema, "transaction", "daily_transaction",
                        "tran_id", "tran_type_cd", "tran_cat_cd", "tran_source", "tran_desc",
                        "tran_amt", "tran_merchant_id", "tran_merchant_name",
                        "tran_merchant_city", "tran_merchant_zip", "tran_card_num",
                        "tran_orig_ts", "tran_proc_ts")));
        readers.add(tableReader("typeReader", "TYPE",
                selectFrom(withSchema, "transaction", "tran_type",
                        "tran_type", "tran_type_desc")));
        readers.add(tableReader("catReader", "CAT",
                selectFrom(withSchema, "transaction", "tran_category",
                        "tran_type_cd", "tran_cat_cd", "tran_cat_type_desc")));
        readers.add(tableReader("balReader", "BAL",
                selectFrom(withSchema, "transaction", "tran_cat_balance",
                        "trancat_acct_id", "trancat_type_cd", "trancat_cd", "tran_cat_bal")));
        readers.add(tableReader("discReader", "DISC",
                selectFrom(withSchema, "transaction", "disclosure_group",
                        "dis_acct_group_id", "dis_tran_type_cd", "dis_tran_cat_cd", "dis_int_rate")));
        return new CompositeItemReader<>(readers);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<String> exportWriter(
            @Value("#{jobParameters['outputFile'] ?: '/tmp/carddemo-export.dat'}") String outputFile) {
        return new FlatFileItemWriterBuilder<String>()
                .name("exportWriter")
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }

    @Bean
    public Step dataExportStep(CompositeItemReader<String> exportCompositeReader,
                               FlatFileItemWriter<String> exportWriter) {
        return new StepBuilder("dataExportStep", jobRepository)
                .<String, String>chunk(1000, transactionManager)
                .reader(exportCompositeReader)
                .writer(exportWriter)
                .build();
    }

    @Bean
    public Job dataExportJob(Step dataExportStep) {
        return new JobBuilder("dataExportJob", jobRepository)
                .start(dataExportStep)
                .build();
    }

    private JdbcCursorItemReader<String> tableReader(String name, String prefix, String sql) {
        return new JdbcCursorItemReaderBuilder<String>()
                .name(name)
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper((rs, rowNum) -> formatRow(prefix, rs))
                .build();
    }

    private String selectFrom(boolean withSchema, String schema, String table, String... columns) {
        String qualifiedTable = withSchema ? schema + "." + table : table;
        return "SELECT " + String.join(", ", columns) + " FROM " + qualifiedTable;
    }

    static String formatRow(String prefix, ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            sb.append('|');
            Object val = rs.getObject(i);
            sb.append(val == null ? "" : val.toString());
        }
        return sb.toString();
    }
}
