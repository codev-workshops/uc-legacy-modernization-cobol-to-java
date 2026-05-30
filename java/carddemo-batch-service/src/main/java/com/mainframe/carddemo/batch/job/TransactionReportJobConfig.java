package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.BatchTransaction;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.format.DateTimeFormatter;

@Configuration
public class TransactionReportJobConfig {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${batch.output.dir:./batch-output}")
    private String outputDir;

    @Bean
    public JpaPagingItemReader<BatchTransaction> transactionReportReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<BatchTransaction>()
                .name("transactionReportReader")
                .entityManagerFactory(emf)
                .queryString("SELECT t FROM BatchTransaction t ORDER BY t.tranCardNum, t.tranOrigTs")
                .pageSize(100)
                .build();
    }

    @Bean
    public FlatFileItemWriter<BatchTransaction> transactionReportWriter() {
        return new FlatFileItemWriterBuilder<BatchTransaction>()
                .name("transactionReportWriter")
                .resource(new FileSystemResource(outputDir + "/transaction-report.txt"))
                .lineAggregator(reportLineAggregator())
                .headerCallback(writer -> writer.write(String.format(
                        "%-16s %-2s %-4s %-12s %-16s %-50s %-20s",
                        "TRAN_ID", "TC", "CAT", "AMOUNT", "CARD_NUM", "MERCHANT", "DATE")))
                .build();
    }

    private LineAggregator<BatchTransaction> reportLineAggregator() {
        return item -> String.format("%-16s %-2s %-4s %12s %-16s %-50s %-20s",
                item.getTranId(),
                item.getTranTypeCd() != null ? item.getTranTypeCd() : "",
                item.getTranCatCd() != null ? item.getTranCatCd().toString() : "",
                item.getTranAmt() != null ? item.getTranAmt().toPlainString() : "0.00",
                item.getTranCardNum() != null ? item.getTranCardNum() : "",
                item.getTranMerchantName() != null ? item.getTranMerchantName() : "",
                item.getTranOrigTs() != null ? item.getTranOrigTs().format(TS_FMT) : "");
    }

    @Bean
    public Step transactionReportStep(JobRepository jobRepository,
                                       PlatformTransactionManager txManager,
                                       JpaPagingItemReader<BatchTransaction> transactionReportReader,
                                       FlatFileItemWriter<BatchTransaction> transactionReportWriter) {
        return new StepBuilder("transactionReportStep", jobRepository)
                .<BatchTransaction, BatchTransaction>chunk(100, txManager)
                .reader(transactionReportReader)
                .writer(transactionReportWriter)
                .build();
    }

    @Bean
    public Job transactionReportJob(JobRepository jobRepository, Step transactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(transactionReportStep)
                .build();
    }
}
