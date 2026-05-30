package com.carddemo.account.batch;

import com.carddemo.account.entity.CardXref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static com.carddemo.account.batch.AccountReaderJobConfig.formatLong;
import static com.carddemo.account.batch.AccountReaderJobConfig.padRight;

@Configuration
public class XrefReaderJobConfig {

    private static final Logger log = LoggerFactory.getLogger(XrefReaderJobConfig.class);

    @Bean
    public JdbcCursorItemReader<CardXref> xrefItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<CardXref>()
                .name("xrefItemReader")
                .dataSource(dataSource)
                .sql("SELECT xref_card_num, xref_cust_id, xref_acct_id FROM card_xref ORDER BY xref_card_num")
                .rowMapper((rs, rowNum) -> CardXref.builder()
                        .xrefCardNum(rs.getString("xref_card_num"))
                        .xrefCustId(rs.getLong("xref_cust_id"))
                        .xrefAcctId(rs.getLong("xref_acct_id"))
                        .build())
                .build();
    }

    @Bean
    public FlatFileItemWriter<CardXref> xrefItemWriter() {
        return new FlatFileItemWriterBuilder<CardXref>()
                .name("xrefItemWriter")
                .resource(new FileSystemResource("output/cardxref.dat"))
                .lineAggregator(XrefReaderJobConfig::formatXref)
                .build();
    }

    @Bean
    public Job xrefReaderJob(JobRepository jobRepository, Step xrefReaderStep) {
        return new JobBuilder("xrefReaderJob", jobRepository)
                .start(xrefReaderStep)
                .listener(new JobExecutionListener() {
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        log.info("Xref reader job completed with status: {}. Records written: {}",
                                jobExecution.getStatus(),
                                jobExecution.getStepExecutions().stream()
                                        .mapToLong(se -> se.getWriteCount())
                                        .sum());
                    }
                })
                .build();
    }

    @Bean
    public Step xrefReaderStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               JdbcCursorItemReader<CardXref> xrefItemReader,
                               FlatFileItemWriter<CardXref> xrefItemWriter) {
        return new StepBuilder("xrefReaderStep", jobRepository)
                .<CardXref, CardXref>chunk(100, transactionManager)
                .reader(xrefItemReader)
                .writer(xrefItemWriter)
                .build();
    }

    static String formatXref(CardXref xref) {
        StringBuilder sb = new StringBuilder(50);
        // XREF-CARD-NUM PIC X(16)
        sb.append(padRight(xref.getXrefCardNum(), 16));
        // XREF-CUST-ID PIC 9(09)
        sb.append(formatLong(xref.getXrefCustId(), 9));
        // XREF-ACCT-ID PIC 9(11)
        sb.append(formatLong(xref.getXrefAcctId(), 11));
        // FILLER PIC X(14)
        int remaining = 50 - sb.length();
        if (remaining > 0) {
            sb.append(String.format("%-" + remaining + "s", ""));
        }
        return sb.substring(0, 50);
    }
}
