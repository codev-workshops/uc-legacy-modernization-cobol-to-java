package com.carddemo.account.batch;

import com.carddemo.account.entity.Account;
import com.carddemo.account.entity.Card;
import com.carddemo.account.entity.CardXref;
import com.carddemo.account.entity.Customer;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.account.repository.CardRepository;
import com.carddemo.account.repository.CardXrefRepository;
import com.carddemo.account.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.time.LocalDateTime;

import static com.carddemo.account.batch.FixedWidthParseUtils.parseInt;
import static com.carddemo.account.batch.FixedWidthParseUtils.parseLong;
import static com.carddemo.account.batch.FixedWidthParseUtils.parseSignedDecimal;
import static com.carddemo.account.batch.FixedWidthParseUtils.substring;

@Configuration("dataImportJobConfiguration")
public class DataImportJob {

    private static final Logger log = LoggerFactory.getLogger(DataImportJob.class);

    // --- Job definition ---

    @Bean
    public Job dataImportJob(JobRepository jobRepository,
                             Step importAccountsStep,
                             Step importCustomersStep,
                             Step importCardsStep,
                             Step importCardXrefStep) {
        return new JobBuilder("dataImportJob", jobRepository)
                .start(importAccountsStep)
                .next(importCustomersStep)
                .next(importCardsStep)
                .next(importCardXrefStep)
                .listener(new JobExecutionListener() {
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        log.info("Data import job completed with status: {}",
                                jobExecution.getStatus());
                    }
                })
                .build();
    }

    // --- Account import ---

    @Bean
    @StepScope
    public FlatFileItemReader<Account> accountImportReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        File file = resolveFile(inputFile, "acctdata.txt");
        log.info("Configuring account import reader for file: {}", file.getAbsolutePath());
        return new FlatFileItemReaderBuilder<Account>()
                .name("accountImportReader")
                .resource(new FileSystemResource(file))
                .lineMapper((line, lineNumber) -> mapAccount(line))
                .build();
    }

    @Bean
    public ItemWriter<Account> accountImportWriter(AccountRepository repository) {
        return chunk -> {
            for (Account account : chunk) {
                account.setCreatedAt(LocalDateTime.now());
                account.setUpdatedAt(LocalDateTime.now());
            }
            repository.saveAll(chunk.getItems());
            log.debug("Imported {} account records", chunk.getItems().size());
        };
    }

    @Bean
    public Step importAccountsStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   FlatFileItemReader<Account> accountImportReader,
                                   ItemWriter<Account> accountImportWriter) {
        return new StepBuilder("importAccountsStep", jobRepository)
                .<Account, Account>chunk(100, transactionManager)
                .reader(accountImportReader)
                .writer(accountImportWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .build();
    }

    // --- Customer import ---

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> customerImportReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        File file = resolveFile(inputFile, "custdata.txt");
        log.info("Configuring customer import reader for file: {}", file.getAbsolutePath());
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerImportReader")
                .resource(new FileSystemResource(file))
                .lineMapper((line, lineNumber) -> mapCustomer(line))
                .build();
    }

    @Bean
    public ItemWriter<Customer> customerImportWriter(CustomerRepository repository) {
        return chunk -> {
            for (Customer customer : chunk) {
                customer.setCreatedAt(LocalDateTime.now());
                customer.setUpdatedAt(LocalDateTime.now());
            }
            repository.saveAll(chunk.getItems());
            log.debug("Imported {} customer records", chunk.getItems().size());
        };
    }

    @Bean
    public Step importCustomersStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    FlatFileItemReader<Customer> customerImportReader,
                                    ItemWriter<Customer> customerImportWriter) {
        return new StepBuilder("importCustomersStep", jobRepository)
                .<Customer, Customer>chunk(100, transactionManager)
                .reader(customerImportReader)
                .writer(customerImportWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .build();
    }

    // --- Card import ---

    @Bean
    @StepScope
    public FlatFileItemReader<Card> cardImportReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        File file = resolveFile(inputFile, "carddata.txt");
        log.info("Configuring card import reader for file: {}", file.getAbsolutePath());
        return new FlatFileItemReaderBuilder<Card>()
                .name("cardImportReader")
                .resource(new FileSystemResource(file))
                .lineMapper((line, lineNumber) -> mapCard(line))
                .build();
    }

    @Bean
    public ItemWriter<Card> cardImportWriter(CardRepository repository) {
        return chunk -> {
            for (Card card : chunk) {
                card.setCreatedAt(LocalDateTime.now());
                card.setUpdatedAt(LocalDateTime.now());
            }
            repository.saveAll(chunk.getItems());
            log.debug("Imported {} card records", chunk.getItems().size());
        };
    }

    @Bean
    public Step importCardsStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                FlatFileItemReader<Card> cardImportReader,
                                ItemWriter<Card> cardImportWriter) {
        return new StepBuilder("importCardsStep", jobRepository)
                .<Card, Card>chunk(100, transactionManager)
                .reader(cardImportReader)
                .writer(cardImportWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .build();
    }

    // --- CardXref import ---

    @Bean
    @StepScope
    public FlatFileItemReader<CardXref> cardXrefImportReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        File file = resolveFile(inputFile, "cardxref.txt");
        log.info("Configuring card xref import reader for file: {}", file.getAbsolutePath());
        return new FlatFileItemReaderBuilder<CardXref>()
                .name("cardXrefImportReader")
                .resource(new FileSystemResource(file))
                .lineMapper((line, lineNumber) -> mapCardXref(line))
                .build();
    }

    @Bean
    public ItemWriter<CardXref> cardXrefImportWriter(CardXrefRepository repository) {
        return chunk -> {
            for (CardXref xref : chunk) {
                xref.setCreatedAt(LocalDateTime.now());
            }
            repository.saveAll(chunk.getItems());
            log.debug("Imported {} card xref records", chunk.getItems().size());
        };
    }

    @Bean
    public Step importCardXrefStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   FlatFileItemReader<CardXref> cardXrefImportReader,
                                   ItemWriter<CardXref> cardXrefImportWriter) {
        return new StepBuilder("importCardXrefStep", jobRepository)
                .<CardXref, CardXref>chunk(100, transactionManager)
                .reader(cardXrefImportReader)
                .writer(cardXrefImportWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .build();
    }

    // --- Field mapping ---

    static Account mapAccount(String line) {
        return Account.builder()
                .acctId(parseLong(line, 0, 11))
                .acctActiveStatus(substring(line, 11, 1))
                .acctCurrBal(parseSignedDecimal(line, 12, 12, 2))
                .acctCreditLimit(parseSignedDecimal(line, 24, 12, 2))
                .acctCashCreditLimit(parseSignedDecimal(line, 36, 12, 2))
                .acctOpenDate(substring(line, 48, 10))
                .acctExpirationDate(substring(line, 58, 10))
                .acctReissueDate(substring(line, 68, 10))
                .acctCurrCycCredit(parseSignedDecimal(line, 78, 12, 2))
                .acctCurrCycDebit(parseSignedDecimal(line, 90, 12, 2))
                .acctAddrZip(substring(line, 102, 10))
                .acctGroupId(substring(line, 112, 10))
                .build();
    }

    static Customer mapCustomer(String line) {
        return Customer.builder()
                .custId(parseLong(line, 0, 9))
                .custFirstName(substring(line, 9, 25))
                .custMiddleName(substring(line, 34, 25))
                .custLastName(substring(line, 59, 25))
                .custAddrLine1(substring(line, 84, 50))
                .custAddrLine2(substring(line, 134, 50))
                .custAddrLine3(substring(line, 184, 50))
                .custAddrStateCd(substring(line, 234, 2))
                .custAddrCountryCd(substring(line, 236, 3))
                .custAddrZip(substring(line, 239, 10))
                .custPhoneNum1(substring(line, 249, 15))
                .custPhoneNum2(substring(line, 264, 15))
                .custSsn(parseLong(line, 279, 9))
                .custGovtIssuedId(substring(line, 288, 20))
                .custDob(substring(line, 308, 10))
                .custEftAccountId(substring(line, 318, 10))
                .custPriCardHolderInd(substring(line, 328, 1))
                .custFicoCreditScore(parseInt(line, 329, 3))
                .build();
    }

    static Card mapCard(String line) {
        return Card.builder()
                .cardNum(substring(line, 0, 16))
                .cardAcctId(parseLong(line, 16, 11))
                .cardCvvCd(parseInt(line, 27, 3))
                .cardEmbossedName(substring(line, 30, 50))
                .cardExpirationDate(substring(line, 80, 10))
                .cardActiveStatus(substring(line, 90, 1))
                .build();
    }

    static CardXref mapCardXref(String line) {
        return CardXref.builder()
                .xrefCardNum(substring(line, 0, 16))
                .xrefCustId(parseLong(line, 16, 9))
                .xrefAcctId(parseLong(line, 25, 11))
                .build();
    }

    private static File resolveFile(String inputFile, String defaultFileName) {
        File f = new File(inputFile);
        if (f.isDirectory()) {
            return new File(f, defaultFileName);
        }
        return f;
    }
}
