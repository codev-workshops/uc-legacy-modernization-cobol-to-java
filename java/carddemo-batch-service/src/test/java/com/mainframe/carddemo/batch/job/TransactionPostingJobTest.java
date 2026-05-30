package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.BalanceUpdateDto;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class TransactionPostingJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("transactionPostingJob")
    private Job transactionPostingJob;

    @Autowired
    private DataSource dataSource;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @BeforeEach
    void setUp() throws Exception {
        jobLauncherTestUtils.setJob(transactionPostingJob);
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            conn.createStatement().executeUpdate("DELETE FROM daily_reject");
            conn.createStatement().executeUpdate("DELETE FROM tran_cat_balance");
            conn.createStatement().executeUpdate("DELETE FROM transaction");
            conn.createStatement().executeUpdate("DELETE FROM daily_transaction");
        }
    }

    @Test
    void shouldPostValidTransaction() throws Exception {
        insertWithRawJdbc("T001", "01", 1, new BigDecimal("100.00"), "4111111111111111");

        CardXrefDto xref = new CardXrefDto();
        xref.setCardNum("4111111111111111");
        xref.setAccountId(1L);
        when(accountServiceClient.getXrefByCardNum("4111111111111111")).thenReturn(xref);

        AccountDto account = new AccountDto();
        account.setAccountId(1L);
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("50.00"));
        account.setExpirationDate(LocalDate.of(2030, 12, 31));
        when(accountServiceClient.getInternalAccountById(1L)).thenReturn(account);
        when(accountServiceClient.updateAccountBalance(anyLong(), any(BalanceUpdateDto.class))).thenReturn(account);

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        int txnCount = countRows("transaction");
        assertEquals(1, txnCount);
        int rejectCount = countRows("daily_reject");
        assertEquals(0, rejectCount);
    }

    @Test
    void shouldRejectTransactionWithInvalidCard() throws Exception {
        insertWithRawJdbc("T002", "01", 1, new BigDecimal("100.00"), "0000000000000000");

        when(accountServiceClient.getXrefByCardNum("0000000000000000")).thenReturn(null);

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        int txnCount = countRows("transaction");
        assertEquals(0, txnCount);
        int rejectCount = countRows("daily_reject");
        assertEquals(1, rejectCount);
        int reason = queryInt("SELECT reject_reason FROM daily_reject");
        assertEquals(100, reason);
    }

    @Test
    void shouldRejectTransactionExceedingCreditLimit() throws Exception {
        insertWithRawJdbc("T003", "01", 1, new BigDecimal("6000.00"), "4111111111111111");

        CardXrefDto xref = new CardXrefDto();
        xref.setCardNum("4111111111111111");
        xref.setAccountId(1L);
        when(accountServiceClient.getXrefByCardNum("4111111111111111")).thenReturn(xref);

        AccountDto account = new AccountDto();
        account.setAccountId(1L);
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("50.00"));
        account.setExpirationDate(LocalDate.of(2030, 12, 31));
        when(accountServiceClient.getInternalAccountById(1L)).thenReturn(account);

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        int rejectCount = countRows("daily_reject");
        assertEquals(1, rejectCount);
        int reason = queryInt("SELECT reject_reason FROM daily_reject");
        assertEquals(102, reason);
    }

    @Test
    void inputCountEqualsProcessedPlusRejected() throws Exception {
        insertWithRawJdbc("T010", "01", 1, new BigDecimal("100.00"), "4111111111111111");
        insertWithRawJdbc("T011", "01", 1, new BigDecimal("200.00"), "0000000000000000");

        CardXrefDto xref = new CardXrefDto();
        xref.setCardNum("4111111111111111");
        xref.setAccountId(1L);
        when(accountServiceClient.getXrefByCardNum("4111111111111111")).thenReturn(xref);
        when(accountServiceClient.getXrefByCardNum("0000000000000000")).thenReturn(null);

        AccountDto account = new AccountDto();
        account.setAccountId(1L);
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("50.00"));
        account.setExpirationDate(LocalDate.of(2030, 12, 31));
        when(accountServiceClient.getInternalAccountById(1L)).thenReturn(account);
        when(accountServiceClient.updateAccountBalance(anyLong(), any(BalanceUpdateDto.class))).thenReturn(account);

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        int inputCount = countRows("daily_transaction");
        int processedCount = countRows("transaction");
        int rejectedCount = countRows("daily_reject");
        assertEquals(inputCount, processedCount + rejectedCount, "Rule 1: input = processed + rejected");
    }

    private void insertWithRawJdbc(String tranId, String typeCd, int catCd,
                                    BigDecimal amount, String cardNum) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO daily_transaction (tran_id, tran_type_cd, tran_cat_cd, tran_amt, " +
                            "tran_card_num, tran_orig_ts, tran_source, tran_desc) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, tranId);
                ps.setString(2, typeCd);
                ps.setInt(3, catCd);
                ps.setBigDecimal(4, amount);
                ps.setString(5, cardNum);
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.of(2024, 6, 15, 10, 0)));
                ps.setString(7, "POS TERM");
                ps.setString(8, "Test transaction");
                ps.executeUpdate();
            }
        }
    }

    private int countRows(String table) throws Exception {
        try (Connection conn = dataSource.getConnection();
             var rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private int queryInt(String sql) throws Exception {
        try (Connection conn = dataSource.getConnection();
             var rs = conn.createStatement().executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
