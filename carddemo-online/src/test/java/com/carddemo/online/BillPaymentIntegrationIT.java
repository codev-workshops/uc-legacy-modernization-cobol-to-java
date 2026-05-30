package com.carddemo.online;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.entity.User;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TransactionRepository;
import com.carddemo.common.repository.UserRepository;
import com.carddemo.online.dto.BillPaymentRequest;
import com.carddemo.online.dto.LoginRequest;
import com.carddemo.online.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BillPaymentIntegrationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        transactionRepository.deleteAll();
        cardXrefRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setUsrId("USER01");
        user.setFname("Test");
        user.setLname("User");
        user.setPwd("userpwd");
        user.setUsrType("U");
        userRepository.save(user);

        Account account = new Account();
        account.setAcctId(10001L);
        account.setActiveStatus("Y");
        account.setCurrBal(new BigDecimal("1000.00"));
        account.setCurrCycCredit(BigDecimal.ZERO);
        account.setCurrCycDebit(BigDecimal.ZERO);
        account.setCreditLimit(new BigDecimal("5000.00"));
        accountRepository.save(account);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setAcctId(10001L);
        xref.setCustId(1L);
        cardXrefRepository.save(xref);

        LoginRequest loginReq = new LoginRequest("USER01", "userpwd");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResp = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);
        userToken = loginResp.getToken();
    }

    @Test
    void payBill_reducesBalanceAndCreatesTransaction() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("300.00"), "ONLINE");

        mockMvc.perform(post("/api/bills/pay")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(10001))
                .andExpect(jsonPath("$.paymentAmount").value(300.00))
                .andExpect(jsonPath("$.newBalance").value(700.00))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.message").value("Bill payment processed successfully"));

        Account updated = accountRepository.findById(10001L).orElseThrow();
        assertEquals(0, new BigDecimal("700.00").compareTo(updated.getCurrBal()));
        assertEquals(0, new BigDecimal("300.00").compareTo(updated.getCurrCycCredit()));

        List<Transaction> txns = transactionRepository.findAll();
        assertFalse(txns.isEmpty());
        Transaction txn = txns.get(0);
        assertEquals("02", txn.getTypeCd());
        assertEquals(2, txn.getCatCd());
        assertEquals("BILL PAYMENT - ONLINE", txn.getDesc());
        assertEquals(0, new BigDecimal("300.00").compareTo(txn.getAmt()));
        assertEquals("4111111111111111", txn.getCardNum());
    }

    @Test
    void payBill_fullBalance_setsBalanceToZero() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("1000.00"), "ONLINE");

        mockMvc.perform(post("/api/bills/pay")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newBalance").value(0.00));

        Account updated = accountRepository.findById(10001L).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getCurrBal()));
    }

    @Test
    void payBill_accountNotFound_returns404() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest(
                99999L, new BigDecimal("100.00"), "ONLINE");

        mockMvc.perform(post("/api/bills/pay")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account ID not found: 99999"));
    }

    @Test
    void payBill_inactiveAccount_returns400() throws Exception {
        Account inactive = new Account();
        inactive.setAcctId(10002L);
        inactive.setActiveStatus("N");
        inactive.setCurrBal(new BigDecimal("500.00"));
        accountRepository.save(inactive);

        BillPaymentRequest request = new BillPaymentRequest(
                10002L, new BigDecimal("100.00"), "ONLINE");

        mockMvc.perform(post("/api/bills/pay")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Account is not active: 10002"));
    }

    @Test
    void payBill_unauthenticated_returns401() throws Exception {
        BillPaymentRequest request = new BillPaymentRequest(
                10001L, new BigDecimal("100.00"), "ONLINE");

        mockMvc.perform(post("/api/bills/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void payBill_multiplePayments_incrementTranId() throws Exception {
        BillPaymentRequest request1 = new BillPaymentRequest(
                10001L, new BigDecimal("100.00"), "ONLINE");
        BillPaymentRequest request2 = new BillPaymentRequest(
                10001L, new BigDecimal("200.00"), "MOBILE");

        mockMvc.perform(post("/api/bills/pay")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/bills/pay")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        List<Transaction> txns = transactionRepository.findAll();
        assertEquals(2, txns.size());

        Account updated = accountRepository.findById(10001L).orElseThrow();
        assertEquals(0, new BigDecimal("700.00").compareTo(updated.getCurrBal()));
    }
}
