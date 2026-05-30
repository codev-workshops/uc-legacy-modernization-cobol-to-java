package com.carddemo.transaction.batch;

import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.dto.CardXrefDto;
import com.carddemo.transaction.entity.DailyReject;
import com.carddemo.transaction.entity.DailyTransaction;
import com.carddemo.transaction.repository.DailyRejectRepository;
import com.carddemo.transaction.repository.DailyTransactionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TransactionValidationJob {

    private final DailyRejectRepository dailyRejectRepository;
    private final DailyTransactionRepository dailyTransactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${carddemo.batch.chunk-size:100}")
    private int chunkSize;

    @Bean
    public Job validationJob(JobRepository jobRepository, Step validationStep) {
        return new JobBuilder("transactionValidationJob", jobRepository)
                .start(validationStep)
                .build();
    }

    @Bean
    public Step validationStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
        return new StepBuilder("validationStep", jobRepository)
                .<DailyTransaction, DailyTransaction>chunk(chunkSize, transactionManager)
                .reader(validationReader())
                .processor(validationProcessor())
                .writer(validationWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<DailyTransaction> validationReader() {
        return new JpaPagingItemReaderBuilder<DailyTransaction>()
                .name("dailyTransactionReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DailyTransaction d ORDER BY d.dalytranId")
                .pageSize(chunkSize)
                .build();
    }

    @Bean
    public ItemProcessor<DailyTransaction, DailyTransaction> validationProcessor() {
        return new ValidationProcessor();
    }

    @Bean
    public ItemWriter<DailyTransaction> validationWriter() {
        return items -> {
            for (DailyTransaction dt : items) {
                dailyTransactionRepository.save(dt);
                log.info("Validated transaction: {}", dt.getDalytranId());
            }
        };
    }

    public class ValidationProcessor implements ItemProcessor<DailyTransaction, DailyTransaction> {

        @Override
        @CircuitBreaker(name = "accountService")
        public DailyTransaction process(DailyTransaction item) {
            String cardNum = item.getDalytranCardNum();
            log.debug("Validating transaction {} for card {}", item.getDalytranId(), cardNum);

            try {
                CardXrefDto xref = accountServiceClient.getCardXref(cardNum);
                if (xref == null) {
                    rejectTransaction(item, "Card cross-reference not found for card: " + cardNum);
                    return null;
                }

                AccountDto account = accountServiceClient.getAccount(xref.getXrefAcctId());
                if (account == null) {
                    rejectTransaction(item, "Account not found for account ID: " + xref.getXrefAcctId());
                    return null;
                }

                if (!"Y".equals(account.getAcctActiveStatus())) {
                    rejectTransaction(item, "Account is not active: " + account.getAcctId());
                    return null;
                }

                if (isCardExpired(account.getAcctExpirationDate())) {
                    rejectTransaction(item, "Card expired for account: " + account.getAcctId());
                    return null;
                }

                item.setDalytranProcTs(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")));
                return item;

            } catch (Exception e) {
                log.error("Error validating transaction {}: {}", item.getDalytranId(), e.getMessage());
                rejectTransaction(item, "Validation error: " + e.getMessage());
                return null;
            }
        }
    }

    static boolean isCardExpired(String expirationDate) {
        if (expirationDate == null || expirationDate.isBlank()) {
            return false;
        }
        try {
            LocalDate expDate = LocalDate.parse(expirationDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return expDate.isBefore(LocalDate.now());
        } catch (Exception e) {
            log.warn("Could not parse expiration date: {}", expirationDate);
            return false;
        }
    }

    private void rejectTransaction(DailyTransaction item, String reason) {
        log.warn("Rejecting transaction {}: {}", item.getDalytranId(), reason);
        DailyReject reject = DailyReject.builder()
                .dalytranId(item.getDalytranId())
                .rejectReason(reason.length() > 100 ? reason.substring(0, 100) : reason)
                .rejectedAt(LocalDateTime.now())
                .build();
        dailyRejectRepository.save(reject);
    }
}
