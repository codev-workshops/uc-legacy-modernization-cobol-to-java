package com.carddemo.batch.importer;

import com.carddemo.batch.export.ExportRecord;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.common.repository.TranCatBalanceRepository;
import com.carddemo.common.repository.TransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job that reads the polymorphic export file and writes records
 * to the corresponding JPA tables. Mirrors CBIMPORT.cbl.
 */
@Configuration
public class DataImportJobConfig {

    private static final int CHUNK_SIZE = 100;

    private final String exportFilePath;

    public DataImportJobConfig(
            @Value("${carddemo.export.file-path:carddemo-export.dat}") String exportFilePath) {
        this.exportFilePath = exportFilePath;
    }

    @Bean
    public Job dataImportJob(JobRepository jobRepository,
                             PlatformTransactionManager txManager,
                             CustomerRepository customerRepo,
                             AccountRepository accountRepo,
                             CardXrefRepository cardXrefRepo,
                             TransactionRepository transactionRepo,
                             CardRepository cardRepo,
                             TranCatBalanceRepository tranCatBalanceRepo) {
        return new JobBuilder("dataImportJob", jobRepository)
                .start(importStep(jobRepository, txManager,
                        customerRepo, accountRepo, cardXrefRepo,
                        transactionRepo, cardRepo, tranCatBalanceRepo))
                .build();
    }

    private Step importStep(JobRepository jobRepository,
                            PlatformTransactionManager txManager,
                            CustomerRepository customerRepo,
                            AccountRepository accountRepo,
                            CardXrefRepository cardXrefRepo,
                            TransactionRepository transactionRepo,
                            CardRepository cardRepo,
                            TranCatBalanceRepository tranCatBalanceRepo) {

        FlatFileItemReader<ExportRecord> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(exportFilePath));
        reader.setLineMapper(new ExportRecordLineMapper());
        reader.setName("importFileReader");

        ImportRecordWriter writer = new ImportRecordWriter(
                customerRepo, accountRepo, cardXrefRepo,
                transactionRepo, cardRepo, tranCatBalanceRepo);

        return new StepBuilder("importRecords", jobRepository)
                .<ExportRecord, ExportRecord>chunk(CHUNK_SIZE, txManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    String getExportFilePath() {
        return exportFilePath;
    }
}
