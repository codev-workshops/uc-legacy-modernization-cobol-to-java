package com.carddemo.account.batch;

import com.carddemo.account.entity.Account;
import com.carddemo.account.entity.Card;
import com.carddemo.account.entity.CardXref;
import com.carddemo.account.entity.Customer;
import com.carddemo.common.dto.ExportRecordDto;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;

import static com.carddemo.account.batch.AccountReaderJobConfig.formatLong;
import static com.carddemo.account.batch.AccountReaderJobConfig.formatSignedDecimal;
import static com.carddemo.account.batch.AccountReaderJobConfig.nullSafe;
import static com.carddemo.account.batch.AccountReaderJobConfig.padRight;

@Configuration("dataExportJobConfiguration")
public class DataExportJob {

    private static final Logger log = LoggerFactory.getLogger(DataExportJob.class);

    // --- Job definition ---

    @Bean
    public Job dataExportJob(JobRepository jobRepository,
                             Step exportAccountsStep,
                             Step exportCustomersStep,
                             Step exportCardsStep,
                             Step exportCardXrefStep) {
        return new JobBuilder("dataExportJob", jobRepository)
                .start(exportAccountsStep)
                .next(exportCustomersStep)
                .next(exportCardsStep)
                .next(exportCardXrefStep)
                .listener(new JobExecutionListener() {
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        log.info("Data export job completed with status: {}",
                                jobExecution.getStatus());
                    }
                })
                .build();
    }

    // --- Account export step ---

    @Bean
    public JdbcCursorItemReader<Account> accountExportReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Account>()
                .name("accountExportReader")
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
    public ItemProcessor<Account, ExportRecordDto> accountExportProcessor() {
        return account -> ExportRecordDto.builder()
                .recordType("A")
                .recordData(formatAccountData(account))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ExportRecordDto> exportAccountWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {
        return new FlatFileItemWriterBuilder<ExportRecordDto>()
                .name("exportAccountWriter")
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(new ExportRecordLineAggregator())
                .build();
    }

    @Bean
    public Step exportAccountsStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   JdbcCursorItemReader<Account> accountExportReader,
                                   ItemProcessor<Account, ExportRecordDto> accountExportProcessor,
                                   FlatFileItemWriter<ExportRecordDto> exportAccountWriter) {
        return new StepBuilder("exportAccountsStep", jobRepository)
                .<Account, ExportRecordDto>chunk(100, transactionManager)
                .reader(accountExportReader)
                .processor(accountExportProcessor)
                .writer(exportAccountWriter)
                .build();
    }

    // --- Customer export step ---

    @Bean
    public JdbcCursorItemReader<Customer> customerExportReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerExportReader")
                .dataSource(dataSource)
                .sql("SELECT cust_id, cust_first_name, cust_middle_name, cust_last_name, " +
                        "cust_addr_line_1, cust_addr_line_2, cust_addr_line_3, " +
                        "cust_addr_state_cd, cust_addr_country_cd, cust_addr_zip, " +
                        "cust_phone_num_1, cust_phone_num_2, cust_ssn, cust_govt_issued_id, " +
                        "cust_dob, cust_eft_account_id, cust_pri_card_holder_ind, " +
                        "cust_fico_credit_score FROM customers ORDER BY cust_id")
                .rowMapper((rs, rowNum) -> Customer.builder()
                        .custId(rs.getLong("cust_id"))
                        .custFirstName(rs.getString("cust_first_name"))
                        .custMiddleName(rs.getString("cust_middle_name"))
                        .custLastName(rs.getString("cust_last_name"))
                        .custAddrLine1(rs.getString("cust_addr_line_1"))
                        .custAddrLine2(rs.getString("cust_addr_line_2"))
                        .custAddrLine3(rs.getString("cust_addr_line_3"))
                        .custAddrStateCd(rs.getString("cust_addr_state_cd"))
                        .custAddrCountryCd(rs.getString("cust_addr_country_cd"))
                        .custAddrZip(rs.getString("cust_addr_zip"))
                        .custPhoneNum1(rs.getString("cust_phone_num_1"))
                        .custPhoneNum2(rs.getString("cust_phone_num_2"))
                        .custSsn(rs.getObject("cust_ssn") != null ? rs.getLong("cust_ssn") : null)
                        .custGovtIssuedId(rs.getString("cust_govt_issued_id"))
                        .custDob(rs.getString("cust_dob"))
                        .custEftAccountId(rs.getString("cust_eft_account_id"))
                        .custPriCardHolderInd(rs.getString("cust_pri_card_holder_ind"))
                        .custFicoCreditScore(rs.getObject("cust_fico_credit_score") != null ?
                                rs.getInt("cust_fico_credit_score") : null)
                        .build())
                .build();
    }

    @Bean
    public ItemProcessor<Customer, ExportRecordDto> customerExportProcessor() {
        return customer -> ExportRecordDto.builder()
                .recordType("U")
                .recordData(formatCustomerData(customer))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ExportRecordDto> exportCustomerWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {
        return new FlatFileItemWriterBuilder<ExportRecordDto>()
                .name("exportCustomerWriter")
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(new ExportRecordLineAggregator())
                .append(true)
                .build();
    }

    @Bean
    public Step exportCustomersStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    JdbcCursorItemReader<Customer> customerExportReader,
                                    ItemProcessor<Customer, ExportRecordDto> customerExportProcessor,
                                    FlatFileItemWriter<ExportRecordDto> exportCustomerWriter) {
        return new StepBuilder("exportCustomersStep", jobRepository)
                .<Customer, ExportRecordDto>chunk(100, transactionManager)
                .reader(customerExportReader)
                .processor(customerExportProcessor)
                .writer(exportCustomerWriter)
                .build();
    }

    // --- Card export step ---

    @Bean
    public JdbcCursorItemReader<Card> cardExportReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Card>()
                .name("cardExportReader")
                .dataSource(dataSource)
                .sql("SELECT card_num, card_acct_id, card_cvv_cd, card_embossed_name, " +
                        "card_expiration_date, card_active_status FROM cards ORDER BY card_num")
                .rowMapper((rs, rowNum) -> Card.builder()
                        .cardNum(rs.getString("card_num"))
                        .cardAcctId(rs.getLong("card_acct_id"))
                        .cardCvvCd(rs.getObject("card_cvv_cd", Integer.class))
                        .cardEmbossedName(rs.getString("card_embossed_name"))
                        .cardExpirationDate(rs.getString("card_expiration_date"))
                        .cardActiveStatus(rs.getString("card_active_status"))
                        .build())
                .build();
    }

    @Bean
    public ItemProcessor<Card, ExportRecordDto> cardExportProcessor() {
        return card -> ExportRecordDto.builder()
                .recordType("C")
                .recordData(formatCardData(card))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ExportRecordDto> exportCardWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {
        return new FlatFileItemWriterBuilder<ExportRecordDto>()
                .name("exportCardWriter")
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(new ExportRecordLineAggregator())
                .append(true)
                .build();
    }

    @Bean
    public Step exportCardsStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                JdbcCursorItemReader<Card> cardExportReader,
                                ItemProcessor<Card, ExportRecordDto> cardExportProcessor,
                                FlatFileItemWriter<ExportRecordDto> exportCardWriter) {
        return new StepBuilder("exportCardsStep", jobRepository)
                .<Card, ExportRecordDto>chunk(100, transactionManager)
                .reader(cardExportReader)
                .processor(cardExportProcessor)
                .writer(exportCardWriter)
                .build();
    }

    // --- CardXref export step ---

    @Bean
    public JdbcCursorItemReader<CardXref> cardXrefExportReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<CardXref>()
                .name("cardXrefExportReader")
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
    public ItemProcessor<CardXref, ExportRecordDto> cardXrefExportProcessor() {
        return xref -> ExportRecordDto.builder()
                .recordType("X")
                .recordData(formatCardXrefData(xref))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ExportRecordDto> exportCardXrefWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {
        return new FlatFileItemWriterBuilder<ExportRecordDto>()
                .name("exportCardXrefWriter")
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(new ExportRecordLineAggregator())
                .append(true)
                .build();
    }

    @Bean
    public Step exportCardXrefStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   JdbcCursorItemReader<CardXref> cardXrefExportReader,
                                   ItemProcessor<CardXref, ExportRecordDto> cardXrefExportProcessor,
                                   FlatFileItemWriter<ExportRecordDto> exportCardXrefWriter) {
        return new StepBuilder("exportCardXrefStep", jobRepository)
                .<CardXref, ExportRecordDto>chunk(100, transactionManager)
                .reader(cardXrefExportReader)
                .processor(cardXrefExportProcessor)
                .writer(exportCardXrefWriter)
                .build();
    }

    // --- Formatting helpers ---

    static String formatAccountData(Account account) {
        StringBuilder sb = new StringBuilder(299);
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
        return sb.toString();
    }

    static String formatCustomerData(Customer customer) {
        StringBuilder sb = new StringBuilder(299);
        sb.append(formatLong(customer.getCustId(), 9));
        sb.append(padRight(customer.getCustFirstName(), 25));
        sb.append(padRight(customer.getCustMiddleName(), 25));
        sb.append(padRight(customer.getCustLastName(), 25));
        sb.append(padRight(customer.getCustAddrLine1(), 50));
        sb.append(padRight(customer.getCustAddrLine2(), 50));
        sb.append(padRight(customer.getCustAddrLine3(), 50));
        sb.append(padRight(customer.getCustAddrStateCd(), 2));
        sb.append(padRight(customer.getCustAddrCountryCd(), 3));
        sb.append(padRight(customer.getCustAddrZip(), 10));
        sb.append(padRight(customer.getCustPhoneNum1(), 15));
        sb.append(padRight(customer.getCustPhoneNum2(), 15));
        sb.append(formatLong(customer.getCustSsn(), 9));
        sb.append(padRight(customer.getCustGovtIssuedId(), 20));
        sb.append(padRight(customer.getCustDob(), 10));
        sb.append(padRight(customer.getCustEftAccountId(), 10));
        sb.append(padRight(customer.getCustPriCardHolderInd(), 1));
        sb.append(String.format("%03d", customer.getCustFicoCreditScore() != null ?
                customer.getCustFicoCreditScore() : 0));
        return sb.toString();
    }

    static String formatCardData(Card card) {
        StringBuilder sb = new StringBuilder(299);
        sb.append(padRight(card.getCardNum(), 16));
        sb.append(formatLong(card.getCardAcctId(), 11));
        sb.append(String.format("%03d", card.getCardCvvCd() != null ? card.getCardCvvCd() : 0));
        sb.append(padRight(card.getCardEmbossedName(), 50));
        sb.append(padRight(card.getCardExpirationDate(), 10));
        sb.append(padRight(card.getCardActiveStatus(), 1));
        return sb.toString();
    }

    static String formatCardXrefData(CardXref xref) {
        StringBuilder sb = new StringBuilder(299);
        sb.append(padRight(xref.getXrefCardNum(), 16));
        sb.append(formatLong(xref.getXrefCustId(), 9));
        sb.append(formatLong(xref.getXrefAcctId(), 11));
        return sb.toString();
    }
}
