package com.carddemo.transaction.batch;

import com.carddemo.common.client.AccountServiceClient;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.dto.CardXrefDto;
import com.carddemo.transaction.entity.DailyReject;
import com.carddemo.transaction.entity.DailyTransaction;
import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.repository.DailyRejectRepository;
import com.carddemo.transaction.repository.TransactionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TransactionPostingJob {

    private final TransactionRepository transactionRepository;
    private final DailyRejectRepository dailyRejectRepository;
    private final AccountServiceClient accountServiceClient;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${carddemo.batch.chunk-size:100}")
    private int chunkSize;

    @Bean
    public Job postingJob(JobRepository jobRepository, Step postingStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(postingStep)
                .build();
    }

    @Bean
    public Step postingStep(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager) {
        return new StepBuilder("postingStep", jobRepository)
                .<DailyTransaction, Transaction>chunk(chunkSize, transactionManager)
                .reader(postingReader())
                .processor(postingProcessor())
                .writer(postingWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<DailyTransaction> postingReader() {
        return new JpaPagingItemReaderBuilder<DailyTransaction>()
                .name("validatedTransactionReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DailyTransaction d WHERE d.dalytranProcTs IS NOT NULL ORDER BY d.dalytranId")
                .pageSize(chunkSize)
                .build();
    }

    @Bean
    public ItemProcessor<DailyTransaction, Transaction> postingProcessor() {
        return new PostingProcessor();
    }

    @Bean
    public ItemWriter<Transaction> postingWriter() {
        return items -> {
            for (Transaction t : items) {
                transactionRepository.save(t);
                log.info("Posted transaction: {}", t.getTranId());
            }
        };
    }

    public class PostingProcessor implements ItemProcessor<DailyTransaction, Transaction> {

        @Override
        @CircuitBreaker(name = "accountService")
        public Transaction process(DailyTransaction item) {
            log.debug("Posting transaction {}", item.getDalytranId());

            try {
                CardXrefDto xref = accountServiceClient.getCardXref(item.getDalytranCardNum());
                if (xref == null) {
                    rejectTransaction(item, "Card cross-reference not found");
                    return null;
                }

                AccountDto account = accountServiceClient.getAccount(xref.getXrefAcctId());
                if (account == null) {
                    rejectTransaction(item, "Account not found");
                    return null;
                }

                if (isCardExpired(account.getAcctExpirationDate())) {
                    rejectTransaction(item, "Card expired for account: " + account.getAcctId());
                    return null;
                }

                if (!isWithinCreditLimit(account, item.getDalytranAmt())) {
                    rejectTransaction(item, "Transaction exceeds credit limit for account: " + account.getAcctId());
                    return null;
                }

                updateAccountBalances(account, item);

                return Transaction.builder()
                        .tranId(item.getDalytranId())
                        .tranTypeCd(item.getDalytranTypeCd())
                        .tranCatCd(item.getDalytranCatCd())
                        .tranSource(item.getDalytranSource())
                        .tranDesc(item.getDalytranDesc())
                        .tranAmt(item.getDalytranAmt())
                        .tranMerchantId(item.getDalytranMerchantId())
                        .tranMerchantName(item.getDalytranMerchantName())
                        .tranMerchantCity(item.getDalytranMerchantCity())
                        .tranMerchantZip(item.getDalytranMerchantZip())
                        .tranCardNum(item.getDalytranCardNum())
                        .tranOrigTs(item.getDalytranOrigTs())
                        .tranProcTs(LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")))
                        .createdAt(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("Error posting transaction {}: {}", item.getDalytranId(), e.getMessage());
                rejectTransaction(item, "Posting error: " + e.getMessage());
                return null;
            }
        }
    }

    static boolean isWithinCreditLimit(AccountDto account, BigDecimal tranAmt) {
        BigDecimal creditLimit = account.getAcctCreditLimit() != null
                ? account.getAcctCreditLimit() : BigDecimal.ZERO;
        BigDecimal currCycCredit = account.getAcctCurrCycCredit() != null
                ? account.getAcctCurrCycCredit() : BigDecimal.ZERO;
        BigDecimal currCycDebit = account.getAcctCurrCycDebit() != null
                ? account.getAcctCurrCycDebit() : BigDecimal.ZERO;
        BigDecimal amount = tranAmt != null ? tranAmt : BigDecimal.ZERO;

        return creditLimit.compareTo(currCycCredit.subtract(currCycDebit).add(amount)) >= 0;
    }

    static boolean isCardExpired(String expirationDate) {
        return TransactionValidationJob.isCardExpired(expirationDate);
    }

    private void updateAccountBalances(AccountDto account, DailyTransaction item) {
        String typeCd = item.getDalytranTypeCd();
        BigDecimal amount = item.getDalytranAmt() != null ? item.getDalytranAmt() : BigDecimal.ZERO;

        if ("02".equals(typeCd) || "03".equals(typeCd)) {
            BigDecimal currCredit = account.getAcctCurrCycCredit() != null
                    ? account.getAcctCurrCycCredit() : BigDecimal.ZERO;
            account.setAcctCurrCycCredit(currCredit.add(amount));
        } else {
            BigDecimal currDebit = account.getAcctCurrCycDebit() != null
                    ? account.getAcctCurrCycDebit() : BigDecimal.ZERO;
            account.setAcctCurrCycDebit(currDebit.add(amount));
        }

        accountServiceClient.updateAccount(account.getAcctId(), account);
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
