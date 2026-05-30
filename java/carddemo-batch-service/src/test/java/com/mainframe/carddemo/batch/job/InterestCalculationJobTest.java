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
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class InterestCalculationJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("interestCalculationJob")
    private Job interestCalculationJob;

    @Autowired
    private DataSource dataSource;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @BeforeEach
    void setUp() throws Exception {
        jobLauncherTestUtils.setJob(interestCalculationJob);
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            conn.createStatement().executeUpdate("DELETE FROM transaction");
            conn.createStatement().executeUpdate("DELETE FROM tran_cat_balance");
            conn.createStatement().executeUpdate("DELETE FROM disclosure_group");
        }
    }

    @Test
    void shouldCalculateInterestAndPostTransaction() throws Exception {
        insertTranCatBalance(10000000001L, "01", 1, new BigDecimal("1200.00"));
        insertDisclosureGroup("GRP001", "01", 1, new BigDecimal("12.00"));

        AccountDto account = buildAccount(10000000001L, "GRP001", new BigDecimal("5000.00"));
        when(accountServiceClient.getInternalAccountById(10000000001L)).thenReturn(account);

        CardXrefDto xref = new CardXrefDto();
        xref.setCardNum("4111111111111111");
        xref.setAccountId(10000000001L);
        when(accountServiceClient.getXrefByAccountId(10000000001L)).thenReturn(List.of(xref));
        when(accountServiceClient.updateAccountBalance(anyLong(), any(BalanceUpdateDto.class))).thenReturn(account);

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        int txnCount = countRows("transaction");
        assertEquals(1, txnCount);

        // monthly_interest = (1200.00 * 12.00) / 1200 = 12.00
        BigDecimal txnAmt = queryDecimal("SELECT tran_amt FROM transaction");
        assertEquals(0, new BigDecimal("12.00").compareTo(txnAmt));
    }

    @Test
    void shouldVerifyInterestFormula() throws Exception {
        // cat_bal=2400, rate=18.00 => monthly_interest = (2400 * 18) / 1200 = 36.00
        insertTranCatBalance(10000000002L, "02", 3, new BigDecimal("2400.00"));
        insertDisclosureGroup("GRP002", "02", 3, new BigDecimal("18.00"));

        AccountDto account = buildAccount(10000000002L, "GRP002", new BigDecimal("1000.00"));
        when(accountServiceClient.getInternalAccountById(10000000002L)).thenReturn(account);

        CardXrefDto xref = new CardXrefDto();
        xref.setCardNum("5222222222222222");
        xref.setAccountId(10000000002L);
        when(accountServiceClient.getXrefByAccountId(10000000002L)).thenReturn(List.of(xref));
        when(accountServiceClient.updateAccountBalance(anyLong(), any(BalanceUpdateDto.class))).thenReturn(account);

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        BigDecimal txnAmt = queryDecimal("SELECT tran_amt FROM transaction");
        assertEquals(0, new BigDecimal("36.00").compareTo(txnAmt));
    }

    @Test
    void shouldResetCycleFieldsAfterInterestPosting() throws Exception {
        insertTranCatBalance(10000000003L, "01", 1, new BigDecimal("600.00"));
        insertDisclosureGroup("GRP003", "01", 1, new BigDecimal("24.00"));

        AccountDto account = buildAccount(10000000003L, "GRP003", new BigDecimal("3000.00"));
        account.setCurrentCycleCredit(new BigDecimal("500.00"));
        account.setCurrentCycleDebit(new BigDecimal("200.00"));
        when(accountServiceClient.getInternalAccountById(10000000003L)).thenReturn(account);

        CardXrefDto xref = new CardXrefDto();
        xref.setCardNum("6333333333333333");
        xref.setAccountId(10000000003L);
        when(accountServiceClient.getXrefByAccountId(10000000003L)).thenReturn(List.of(xref));
        when(accountServiceClient.updateAccountBalance(anyLong(), any(BalanceUpdateDto.class))).thenReturn(account);

        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        // Verify balance update was called with cycle fields reset to zero
        // monthly_interest = (600 * 24) / 1200 = 12.00
        // new balance = 3000.00 + 12.00 = 3012.00
        verify(accountServiceClient).updateAccountBalance(eq(10000000003L), any(BalanceUpdateDto.class));
    }

    @Test
    void shouldFallbackToDefaultGroupWhenAccountGroupMissing() throws Exception {
        insertTranCatBalance(10000000004L, "01", 1, new BigDecimal("1200.00"));
        insertDisclosureGroup("DEFAULT", "01", 1, new BigDecimal("6.00"));

        AccountDto account = buildAccount(10000000004L, "UNKNOWN", new BigDecimal("2000.00"));
        when(accountServiceClient.getInternalAccountById(10000000004L)).thenReturn(account);

        CardXrefDto xref = new CardXrefDto();
        xref.setCardNum("7444444444444444");
        xref.setAccountId(10000000004L);
        when(accountServiceClient.getXrefByAccountId(10000000004L)).thenReturn(List.of(xref));
        when(accountServiceClient.updateAccountBalance(anyLong(), any(BalanceUpdateDto.class))).thenReturn(account);

        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        // monthly_interest = (1200 * 6) / 1200 = 6.00
        BigDecimal txnAmt = queryDecimal("SELECT tran_amt FROM transaction");
        assertEquals(0, new BigDecimal("6.00").compareTo(txnAmt));
    }

    @Test
    void shouldSkipWhenRateIsZero() throws Exception {
        insertTranCatBalance(10000000005L, "01", 1, new BigDecimal("1200.00"));
        insertDisclosureGroup("GRP005", "01", 1, BigDecimal.ZERO);

        AccountDto account = buildAccount(10000000005L, "GRP005", new BigDecimal("1000.00"));
        when(accountServiceClient.getInternalAccountById(10000000005L)).thenReturn(account);
        when(accountServiceClient.getXrefByAccountId(10000000005L)).thenReturn(List.of());

        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        int txnCount = countRows("transaction");
        assertEquals(0, txnCount);
    }

    @Test
    void shouldHandleMultipleCategoriesForSameAccount() throws Exception {
        insertTranCatBalance(10000000006L, "01", 1, new BigDecimal("1200.00"));
        insertTranCatBalance(10000000006L, "02", 2, new BigDecimal("2400.00"));
        insertDisclosureGroup("GRP006", "01", 1, new BigDecimal("12.00"));
        insertDisclosureGroup("GRP006", "02", 2, new BigDecimal("18.00"));

        AccountDto account = buildAccount(10000000006L, "GRP006", new BigDecimal("4000.00"));
        when(accountServiceClient.getInternalAccountById(10000000006L)).thenReturn(account);

        CardXrefDto xref = new CardXrefDto();
        xref.setCardNum("8555555555555555");
        xref.setAccountId(10000000006L);
        when(accountServiceClient.getXrefByAccountId(10000000006L)).thenReturn(List.of(xref));
        when(accountServiceClient.updateAccountBalance(anyLong(), any(BalanceUpdateDto.class))).thenReturn(account);

        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        int txnCount = countRows("transaction");
        assertEquals(2, txnCount);

        // Cat 1: (1200 * 12) / 1200 = 12.00
        // Cat 2: (2400 * 18) / 1200 = 36.00
        BigDecimal totalAmt = queryDecimal("SELECT SUM(tran_amt) FROM transaction");
        assertEquals(0, new BigDecimal("48.00").compareTo(totalAmt));
    }

    private AccountDto buildAccount(Long acctId, String groupId, BigDecimal balance) {
        AccountDto account = new AccountDto();
        account.setAccountId(acctId);
        account.setGroupId(groupId);
        account.setCurrentBalance(balance);
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        return account;
    }

    private void insertTranCatBalance(Long acctId, String typeCd, int catCd, BigDecimal balance) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO tran_cat_balance (trancat_acct_id, trancat_type_cd, trancat_cd, tran_cat_bal) VALUES (?, ?, ?, ?)")) {
            ps.setLong(1, acctId);
            ps.setString(2, typeCd);
            ps.setInt(3, catCd);
            ps.setBigDecimal(4, balance);
            ps.executeUpdate();
        }
    }

    private void insertDisclosureGroup(String groupId, String typeCd, int catCd, BigDecimal rate) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO disclosure_group (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd, dis_int_rate) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, groupId);
            ps.setString(2, typeCd);
            ps.setInt(3, catCd);
            ps.setBigDecimal(4, rate);
            ps.executeUpdate();
        }
    }

    private int countRows(String table) throws Exception {
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private BigDecimal queryDecimal(String sql) throws Exception {
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }
}
