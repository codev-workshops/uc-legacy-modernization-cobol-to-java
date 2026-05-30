package com.carddemo.account.batch;

import com.carddemo.account.entity.Account;
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
import java.math.BigDecimal;

@Configuration
public class AccountReaderJobConfig {

    private static final Logger log = LoggerFactory.getLogger(AccountReaderJobConfig.class);

    @Bean
    public JdbcCursorItemReader<Account> accountItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Account>()
                .name("accountItemReader")
                .dataSource(dataSource)
                .sql("SELECT acct_id, acct_active_status, acct_curr_bal, acct_credit_limit, " +
                        "acct_cash_credit_limit, acct_open_date, acct_expiration_date, acct_reissue_date, " +
                        "acct_curr_cyc_credit, acct_curr_cyc_debit, acct_addr_zip, acct_group_id " +
                        "FROM accounts ORDER BY acct_id")
                .rowMapper((rs, rowNum) -> Account.builder()
                        .acctId(rs.getLong("acct_id"))
                        .acctActiveStatus(rs.getString("acct_active_status"))
                        .acctCurrBal(rs.getBigDecimal("acct_curr_bal"))
                        .acctCreditLimit(rs.getBigDecimal("acct_credit_limit"))
                        .acctCashCreditLimit(rs.getBigDecimal("acct_cash_credit_limit"))
                        .acctOpenDate(rs.getString("acct_open_date"))
                        .acctExpirationDate(rs.getString("acct_expiration_date"))
                        .acctReissueDate(rs.getString("acct_reissue_date"))
                        .acctCurrCycCredit(rs.getBigDecimal("acct_curr_cyc_credit"))
                        .acctCurrCycDebit(rs.getBigDecimal("acct_curr_cyc_debit"))
                        .acctAddrZip(rs.getString("acct_addr_zip"))
                        .acctGroupId(rs.getString("acct_group_id"))
                        .build())
                .build();
    }

    @Bean
    public FlatFileItemWriter<Account> accountItemWriter() {
        return new FlatFileItemWriterBuilder<Account>()
                .name("accountItemWriter")
                .resource(new FileSystemResource("output/accounts.dat"))
                .lineAggregator(account -> formatAccount(account))
                .build();
    }

    @Bean
    public Job accountReaderJob(JobRepository jobRepository, Step accountReaderStep) {
        return new JobBuilder("accountReaderJob", jobRepository)
                .start(accountReaderStep)
                .listener(new JobExecutionListener() {
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        log.info("Account reader job completed with status: {}. Records written: {}",
                                jobExecution.getStatus(),
                                jobExecution.getStepExecutions().stream()
                                        .mapToLong(se -> se.getWriteCount())
                                        .sum());
                    }
                })
                .build();
    }

    @Bean
    public Step accountReaderStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  JdbcCursorItemReader<Account> accountItemReader,
                                  FlatFileItemWriter<Account> accountItemWriter) {
        return new StepBuilder("accountReaderStep", jobRepository)
                .<Account, Account>chunk(100, transactionManager)
                .reader(accountItemReader)
                .writer(accountItemWriter)
                .build();
    }

    static String formatAccount(Account account) {
        StringBuilder sb = new StringBuilder(300);
        sb.append(padRight(formatLong(account.getAcctId(), 11), 11));
        sb.append(padRight(nullSafe(account.getAcctActiveStatus()), 1));
        sb.append(formatSignedDecimal(account.getAcctCurrBal(), 10, 2));
        sb.append(formatSignedDecimal(account.getAcctCreditLimit(), 10, 2));
        sb.append(formatSignedDecimal(account.getAcctCashCreditLimit(), 10, 2));
        sb.append(padRight(nullSafe(account.getAcctOpenDate()), 10));
        sb.append(padRight(nullSafe(account.getAcctExpirationDate()), 10));
        sb.append(padRight(nullSafe(account.getAcctReissueDate()), 10));
        sb.append(formatSignedDecimal(account.getAcctCurrCycCredit(), 10, 2));
        sb.append(formatSignedDecimal(account.getAcctCurrCycDebit(), 10, 2));
        sb.append(padRight(nullSafe(account.getAcctAddrZip()), 10));
        sb.append(padRight(nullSafe(account.getAcctGroupId()), 10));
        // FILLER to reach 300 bytes
        int remaining = 300 - sb.length();
        if (remaining > 0) {
            sb.append(String.format("%-" + remaining + "s", ""));
        }
        return sb.substring(0, 300);
    }

    static String padRight(String value, int length) {
        if (value == null) {
            return String.format("%-" + length + "s", "");
        }
        if (value.length() >= length) {
            return value.substring(0, length);
        }
        return String.format("%-" + length + "s", value);
    }

    static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    static String formatLong(Long value, int width) {
        if (value == null) {
            return String.format("%" + width + "s", "").replace(' ', '0');
        }
        return String.format("%0" + width + "d", value);
    }

    static String formatSignedDecimal(BigDecimal value, int intDigits, int decDigits) {
        int totalWidth = intDigits + decDigits;
        if (value == null) {
            return String.format("%" + totalWidth + "s", "").replace(' ', '0');
        }
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();
        long unscaled = abs.movePointRight(decDigits).longValue();
        String formatted = String.format("%0" + totalWidth + "d", unscaled);
        if (formatted.length() > totalWidth) {
            formatted = formatted.substring(formatted.length() - totalWidth);
        }
        if (negative) {
            // COBOL signed: last digit gets overpunch. For simplicity, prefix with '-'
            // but match fixed width by using the sign in the numeric representation
            char lastDigit = formatted.charAt(formatted.length() - 1);
            // COBOL overpunch: 0->}, 1->J, 2->K, ..., 9->R
            char[] overpunch = {'}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R'};
            formatted = formatted.substring(0, formatted.length() - 1) + overpunch[lastDigit - '0'];
        }
        return formatted;
    }
}
