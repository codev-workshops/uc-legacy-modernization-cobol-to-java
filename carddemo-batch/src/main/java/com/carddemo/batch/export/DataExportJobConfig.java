package com.carddemo.batch.export;

import com.carddemo.batch.converter.RecordConverter;
import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.common.repository.TranCatBalanceRepository;
import com.carddemo.common.repository.TransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Spring Batch job that exports all core tables to a single polymorphic flat file.
 * Mirrors CBEXPORT.cbl: sequential steps for Customer (C), Account (A),
 * CardXref (X), Transaction (T), Card (D), and TranCatBalance (B).
 */
@Configuration
public class DataExportJobConfig {

    private static final String DEFAULT_BRANCH_ID = "0001";
    private static final String DEFAULT_REGION_CODE = "NORTH";
    private static final int CHUNK_SIZE = 100;
    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.000000");

    private final String exportFilePath;
    private final AtomicLong sequenceCounter = new AtomicLong(0);
    private final AtomicReference<String> timestamp = new AtomicReference<>();

    public DataExportJobConfig(
            @Value("${carddemo.export.file-path:carddemo-export.dat}") String exportFilePath) {
        this.exportFilePath = exportFilePath;
    }

    @Bean
    public Job dataExportJob(JobRepository jobRepository,
                             PlatformTransactionManager txManager,
                             CustomerRepository customerRepo,
                             AccountRepository accountRepo,
                             CardXrefRepository cardXrefRepo,
                             TransactionRepository transactionRepo,
                             CardRepository cardRepo,
                             TranCatBalanceRepository tranCatBalanceRepo) {
        return new JobBuilder("dataExportJob", jobRepository)
                .listener(resetListener())
                .start(exportStep("exportCustomers", jobRepository, txManager,
                        customerRepo, RecordType.CUSTOMER, "custId",
                        RecordConverter::customerToFields, false))
                .next(exportStep("exportAccounts", jobRepository, txManager,
                        accountRepo, RecordType.ACCOUNT, "acctId",
                        RecordConverter::accountToFields, true))
                .next(exportStep("exportCardXrefs", jobRepository, txManager,
                        cardXrefRepo, RecordType.CARD_XREF, "xrefCardNum",
                        RecordConverter::cardXrefToFields, true))
                .next(exportStep("exportTransactions", jobRepository, txManager,
                        transactionRepo, RecordType.TRANSACTION, "tranId",
                        RecordConverter::transactionToFields, true))
                .next(exportStep("exportCards", jobRepository, txManager,
                        cardRepo, RecordType.CARD, "cardNum",
                        RecordConverter::cardToFields, true))
                .next(exportStep("exportTranCatBalances", jobRepository, txManager,
                        tranCatBalanceRepo, RecordType.TRAN_CAT_BALANCE, "acctId",
                        RecordConverter::tranCatBalanceToFields, true))
                .build();
    }

    private <T> Step exportStep(String stepName,
                                JobRepository jobRepository,
                                PlatformTransactionManager txManager,
                                PagingAndSortingRepository<T, ?> repository,
                                RecordType recordType,
                                String sortField,
                                Function<T, String[]> fieldExtractor,
                                boolean append) {
        RepositoryItemReader<T> reader = new RepositoryItemReader<>();
        reader.setRepository(repository);
        reader.setMethodName("findAll");
        reader.setSort(Map.of(sortField, Sort.Direction.ASC));
        reader.setPageSize(CHUNK_SIZE);

        ItemProcessor<T, ExportRecord> processor = entity -> {
            ExportRecord record = new ExportRecord();
            record.setRecordType(recordType);
            record.setTimestamp(timestamp.get());
            record.setSequenceNum(sequenceCounter.incrementAndGet());
            record.setBranchId(DEFAULT_BRANCH_ID);
            record.setRegionCode(DEFAULT_REGION_CODE);
            record.setFields(fieldExtractor.apply(entity));
            return record;
        };

        FlatFileItemWriter<ExportRecord> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(exportFilePath));
        writer.setLineAggregator(new ExportRecordLineAggregator());
        writer.setAppendAllowed(append);
        writer.setShouldDeleteIfExists(!append);
        writer.setName(stepName + "Writer");

        return new StepBuilder(stepName, jobRepository)
                .<T, ExportRecord>chunk(CHUNK_SIZE, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    private JobExecutionListener resetListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                sequenceCounter.set(0);
                timestamp.set(LocalDateTime.now().format(TS_FORMAT));
                try {
                    Files.deleteIfExists(Path.of(exportFilePath));
                } catch (IOException ignored) {
                    // file may not exist yet
                }
            }
        };
    }

    String getExportFilePath() {
        return exportFilePath;
    }
}
