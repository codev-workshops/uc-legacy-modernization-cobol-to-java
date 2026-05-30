package com.carddemo.online;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.User;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.common.repository.UserRepository;
import com.carddemo.online.dto.AccountUpdateRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountIntegrationIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        cardXrefRepository.deleteAll();
        cardRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setUsrId("USER01");
        user.setFname("Regular");
        user.setLname("User");
        user.setPwd("userpwd");
        user.setUsrType("U");
        userRepository.save(user);

        Account acct1 = new Account();
        acct1.setAcctId(1000000001L);
        acct1.setActiveStatus("Y");
        acct1.setCurrBal(new BigDecimal("1500.00"));
        acct1.setCreditLimit(new BigDecimal("5000.00"));
        acct1.setCashCreditLimit(new BigDecimal("1500.00"));
        acct1.setOpenDate("2020-01-15");
        acct1.setExpirationDate("2025-01-15");
        acct1.setAddrZip("60601");
        accountRepository.save(acct1);

        Account acct2 = new Account();
        acct2.setAcctId(1000000002L);
        acct2.setActiveStatus("N");
        acct2.setCurrBal(new BigDecimal("0.00"));
        acct2.setCreditLimit(new BigDecimal("3000.00"));
        acct2.setOpenDate("2019-06-01");
        accountRepository.save(acct2);

        Customer customer = new Customer();
        customer.setCustId(100001L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customerRepository.save(customer);

        Card card = new Card();
        card.setCardNum("4111111111111111");
        card.setAcctId(1000000001L);
        card.setEmbossedName("JOHN DOE");
        card.setActiveStatus("Y");
        card.setExpirationDate("2025-01-15");
        cardRepository.save(card);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setAcctId(1000000001L);
        xref.setCustId(100001L);
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
    void getAccount_withRelatedData() throws Exception {
        mockMvc.perform(get("/api/accounts/1000000001")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctId").value(1000000001))
                .andExpect(jsonPath("$.activeStatus").value("Y"))
                .andExpect(jsonPath("$.currBal").value(1500.00))
                .andExpect(jsonPath("$.cards.length()").value(1))
                .andExpect(jsonPath("$.cards[0].cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.customer.custId").value(100001))
                .andExpect(jsonPath("$.customer.firstName").value("John"));
    }

    @Test
    void getAccount_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/accounts/999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getAccount_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/accounts/1000000001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateAccount_updatesFields() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("N");
        request.setCreditLimit(new BigDecimal("10000.00"));

        mockMvc.perform(put("/api/accounts/1000000001")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeStatus").value("N"))
                .andExpect(jsonPath("$.creditLimit").value(10000.00));

        mockMvc.perform(get("/api/accounts/1000000001")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeStatus").value("N"));
    }

    @Test
    void updateAccount_notFound_returns404() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("Y");

        mockMvc.perform(put("/api/accounts/999999")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAccount_invalidStatus_returns400() throws Exception {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("X");

        mockMvc.perform(put("/api/accounts/1000000001")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listAccounts_allAccounts() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void listAccounts_filterByStatus() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .param("status", "Y")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].activeStatus").value("Y"));
    }

    @Test
    void listAccounts_filterByCustomerId() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .param("customerId", "100001")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].acctId").value(1000000001));
    }

    @Test
    void listAccounts_filterByCustomerIdAndStatus() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .param("customerId", "100001")
                        .param("status", "Y")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listAccounts_filterByCustomerIdNoMatch() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .param("customerId", "999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void listAccounts_paginated() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }
}
