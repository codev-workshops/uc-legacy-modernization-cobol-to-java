package com.carddemo.online;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Transaction;
import com.carddemo.common.entity.User;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.TransactionRepository;
import com.carddemo.common.repository.UserRepository;
import com.carddemo.online.dto.LoginRequest;
import com.carddemo.online.dto.LoginResponse;
import com.carddemo.online.dto.TransactionRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionIntegrationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private TransactionRepository transactionRepository;

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
        user.setFname("Regular");
        user.setLname("User");
        user.setPwd("userpwd");
        user.setUsrType("U");
        userRepository.save(user);

        Account acct = new Account();
        acct.setAcctId(100L);
        acct.setActiveStatus("Y");
        acct.setCurrBal(new BigDecimal("1000.00"));
        acct.setCreditLimit(new BigDecimal("5000.00"));
        acct.setCurrCycCredit(new BigDecimal("500.00"));
        acct.setCurrCycDebit(new BigDecimal("200.00"));
        acct.setExpirationDate("2028-12-31");
        acct.setOpenDate("2020-01-01");
        accountRepository.save(acct);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setCustId(1L);
        xref.setAcctId(100L);
        cardXrefRepository.save(xref);

        Transaction txn = new Transaction();
        txn.setTranId("TXN0000000000001");
        txn.setTypeCd("SA");
        txn.setCatCd(5001);
        txn.setSource("BATCH");
        txn.setDesc("Existing transaction");
        txn.setAmt(new BigDecimal("50.00"));
        txn.setCardNum("4111111111111111");
        txn.setOrigTs("2026-01-15-10.30.00.000000");
        txn.setProcTs("2026-01-15-10.30.00.000000");
        transactionRepository.save(txn);

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
    void listTransactions_returnsAll() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].tranId").value("TXN0000000000001"));
    }

    @Test
    void listTransactions_filterByCardNum() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .param("cardNum", "4111111111111111")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void listTransactions_filterByAccountId() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .param("accountId", "100")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void listTransactions_filterByDateRange() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void listTransactions_filterByDateRange_noMatch() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .param("startDate", "2099-01-01")
                        .param("endDate", "2099-12-31")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void listTransactions_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTransaction_found() throws Exception {
        mockMvc.perform(get("/api/transactions/TXN0000000000001")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranId").value("TXN0000000000001"))
                .andExpect(jsonPath("$.amt").value(50.00))
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"));
    }

    @Test
    void getTransaction_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/transactions/MISSING")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Transaction not found: MISSING"));
    }

    @Test
    void addTransaction_valid_returns201() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("25.50"));
        request.setTypeCd("SA");
        request.setDescription("Online purchase");
        request.setSource("ONLINE");

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tranId").isNotEmpty())
                .andExpect(jsonPath("$.amt").value(25.50))
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.origTs").isNotEmpty());
    }

    @Test
    void addTransaction_overlimit_returns422() throws Exception {
        Account acct = accountRepository.findById(100L).orElseThrow();
        acct.setCreditLimit(new BigDecimal("100.00"));
        acct.setCurrCycCredit(new BigDecimal("90.00"));
        acct.setCurrCycDebit(new BigDecimal("0.00"));
        accountRepository.save(acct);

        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.reasonCode").value(102))
                .andExpect(jsonPath("$.error").value("OVERLIMIT TRANSACTION"));
    }

    @Test
    void addTransaction_expiredAccount_returns422() throws Exception {
        Account acct = accountRepository.findById(100L).orElseThrow();
        acct.setExpirationDate("2020-01-01");
        accountRepository.save(acct);

        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.reasonCode").value(103));
    }

    @Test
    void addTransaction_invalidCard_returns422() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setCardNum("9999999999999999");
        request.setAmt(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.reasonCode").value(100));
    }

    @Test
    void addTransaction_missingFields_returns400() throws Exception {
        TransactionRequest request = new TransactionRequest();

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addTransaction_thenRetrieveById() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setCardNum("4111111111111111");
        request.setAmt(new BigDecimal("75.00"));
        request.setTypeCd("SA");
        request.setDescription("Round-trip test");

        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String tranId = objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("tranId").asText();

        mockMvc.perform(get("/api/transactions/" + tranId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tranId").value(tranId))
                .andExpect(jsonPath("$.amt").value(75.00));
    }

    @Test
    void listTransactions_pagination() throws Exception {
        for (int i = 2; i <= 5; i++) {
            Transaction txn = new Transaction();
            txn.setTranId(String.format("TXN%013d", i));
            txn.setCardNum("4111111111111111");
            txn.setAmt(new BigDecimal("10.00"));
            txn.setOrigTs("2026-01-15-10.30.00.000000");
            transactionRepository.save(txn);
        }

        mockMvc.perform(get("/api/transactions")
                        .param("size", "2")
                        .param("page", "0")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5));
    }
}
