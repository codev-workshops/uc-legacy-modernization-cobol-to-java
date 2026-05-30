package com.carddemo.account.batch;

import com.carddemo.account.entity.Customer;
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
public class CustomerReaderJobConfig {

    private static final Logger log = LoggerFactory.getLogger(CustomerReaderJobConfig.class);

    @Bean
    public JdbcCursorItemReader<Customer> customerItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerItemReader")
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
    public FlatFileItemWriter<Customer> customerItemWriter() {
        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerItemWriter")
                .resource(new FileSystemResource("output/customers.dat"))
                .lineAggregator(CustomerReaderJobConfig::formatCustomer)
                .build();
    }

    @Bean
    public Job customerReaderJob(JobRepository jobRepository, Step customerReaderStep) {
        return new JobBuilder("customerReaderJob", jobRepository)
                .start(customerReaderStep)
                .listener(new JobExecutionListener() {
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        log.info("Customer reader job completed with status: {}. Records written: {}",
                                jobExecution.getStatus(),
                                jobExecution.getStepExecutions().stream()
                                        .mapToLong(se -> se.getWriteCount())
                                        .sum());
                    }
                })
                .build();
    }

    @Bean
    public Step customerReaderStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   JdbcCursorItemReader<Customer> customerItemReader,
                                   FlatFileItemWriter<Customer> customerItemWriter) {
        return new StepBuilder("customerReaderStep", jobRepository)
                .<Customer, Customer>chunk(100, transactionManager)
                .reader(customerItemReader)
                .writer(customerItemWriter)
                .build();
    }

    static String formatCustomer(Customer customer) {
        StringBuilder sb = new StringBuilder(500);
        // CUST-ID PIC 9(09)
        sb.append(formatLong(customer.getCustId(), 9));
        // CUST-FIRST-NAME PIC X(25)
        sb.append(padRight(customer.getCustFirstName(), 25));
        // CUST-MIDDLE-NAME PIC X(25)
        sb.append(padRight(customer.getCustMiddleName(), 25));
        // CUST-LAST-NAME PIC X(25)
        sb.append(padRight(customer.getCustLastName(), 25));
        // CUST-ADDR-LINE-1 PIC X(50)
        sb.append(padRight(customer.getCustAddrLine1(), 50));
        // CUST-ADDR-LINE-2 PIC X(50)
        sb.append(padRight(customer.getCustAddrLine2(), 50));
        // CUST-ADDR-LINE-3 PIC X(50)
        sb.append(padRight(customer.getCustAddrLine3(), 50));
        // CUST-ADDR-STATE-CD PIC X(02)
        sb.append(padRight(customer.getCustAddrStateCd(), 2));
        // CUST-ADDR-COUNTRY-CD PIC X(03)
        sb.append(padRight(customer.getCustAddrCountryCd(), 3));
        // CUST-ADDR-ZIP PIC X(10)
        sb.append(padRight(customer.getCustAddrZip(), 10));
        // CUST-PHONE-NUM-1 PIC X(15)
        sb.append(padRight(customer.getCustPhoneNum1(), 15));
        // CUST-PHONE-NUM-2 PIC X(15)
        sb.append(padRight(customer.getCustPhoneNum2(), 15));
        // CUST-SSN PIC 9(09)
        sb.append(formatLong(customer.getCustSsn(), 9));
        // CUST-GOVT-ISSUED-ID PIC X(20)
        sb.append(padRight(customer.getCustGovtIssuedId(), 20));
        // CUST-DOB-YYYY-MM-DD PIC X(10)
        sb.append(padRight(customer.getCustDob(), 10));
        // CUST-EFT-ACCOUNT-ID PIC X(10)
        sb.append(padRight(customer.getCustEftAccountId(), 10));
        // CUST-PRI-CARD-HOLDER-IND PIC X(01)
        sb.append(padRight(customer.getCustPriCardHolderInd(), 1));
        // CUST-FICO-CREDIT-SCORE PIC 9(03)
        sb.append(String.format("%03d", customer.getCustFicoCreditScore() != null ?
                customer.getCustFicoCreditScore() : 0));
        // FILLER PIC X(168) to reach 500 bytes
        int remaining = 500 - sb.length();
        if (remaining > 0) {
            sb.append(String.format("%-" + remaining + "s", ""));
        }
        return sb.substring(0, 500);
    }
}
