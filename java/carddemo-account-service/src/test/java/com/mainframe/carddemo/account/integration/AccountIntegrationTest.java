package com.mainframe.carddemo.account.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainframe.carddemo.account.entity.Account;
import com.mainframe.carddemo.account.entity.Card;
import com.mainframe.carddemo.account.entity.CardXref;
import com.mainframe.carddemo.account.entity.Customer;
import com.mainframe.carddemo.account.repository.AccountRepository;
import com.mainframe.carddemo.account.repository.CardRepository;
import com.mainframe.carddemo.account.repository.CardXrefRepository;
import com.mainframe.carddemo.account.repository.CustomerRepository;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.BalanceUpdateDto;
import com.mainframe.carddemo.common.dto.CardDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @BeforeEach
    void setUp() {
        cardXrefRepository.deleteAll();
        cardRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();

        Customer customer = new Customer();
        customer.setCustId(100L);
        customer.setCustFirstName("John");
        customer.setCustLastName("Doe");
        customer.setCustAddrZip("90210");
        customerRepository.save(customer);

        Account account = new Account();
        account.setAcctId(1L);
        account.setAcctActiveStatus("Y");
        account.setAcctCurrBal(new BigDecimal("5000.00"));
        account.setAcctCreditLimit(new BigDecimal("10000.00"));
        account.setAcctCashCreditLimit(new BigDecimal("3000.00"));
        account.setAcctOpenDate(LocalDate.of(2020, 1, 15));
        account.setAcctAddrZip("10001");
        account.setAcctGroupId("GRP01");
        accountRepository.save(account);

        Card card = new Card();
        card.setCardNum("4111111111111111");
        card.setCardAcctId(1L);
        card.setCardCvvCd(123);
        card.setCardEmbossedName("JOHN DOE");
        card.setCardExpirationDate(LocalDate.of(2026, 6, 30));
        card.setCardActiveStatus("Y");
        cardRepository.save(card);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setXrefCustId(100L);
        xref.setXrefAcctId(1L);
        cardXrefRepository.save(xref);
    }

    @Test
    void getAccount_fullLifecycle() throws Exception {
        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.activeStatus").value("Y"))
                .andExpect(jsonPath("$.currentBalance").value(5000.00));
    }

    @Test
    void updateAccount_fullLifecycle() throws Exception {
        AccountDto update = new AccountDto();
        update.setActiveStatus("N");
        update.setCreditLimit(new BigDecimal("20000.00"));

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeStatus").value("N"))
                .andExpect(jsonPath("$.creditLimit").value(20000.00));
    }

    @Test
    void getCustomer_fullLifecycle() throws Exception {
        mockMvc.perform(get("/api/customers/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(100))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getCards_fullLifecycle() throws Exception {
        mockMvc.perform(get("/api/cards").param("acctId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cardNum").value("4111111111111111"));
    }

    @Test
    void getCard_fullLifecycle() throws Exception {
        mockMvc.perform(get("/api/cards/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.embossedName").value("JOHN DOE"));
    }

    @Test
    void updateCard_fullLifecycle() throws Exception {
        CardDto update = new CardDto();
        update.setEmbossedName("JANE DOE");
        update.setActiveStatus("N");

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("JANE DOE"))
                .andExpect(jsonPath("$.activeStatus").value("N"));
    }

    @Test
    void internalXref_byCardNum() throws Exception {
        mockMvc.perform(get("/internal/xref/4111111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.customerId").value(100))
                .andExpect(jsonPath("$.accountId").value(1));
    }

    @Test
    void internalXref_byAccountId() throws Exception {
        mockMvc.perform(get("/internal/xref/byAccount/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void internalAccount_get() throws Exception {
        mockMvc.perform(get("/internal/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(1));
    }

    @Test
    void internalAccount_updateBalance() throws Exception {
        BalanceUpdateDto balance = new BalanceUpdateDto();
        balance.setCurrentBalance(new BigDecimal("7500.00"));
        balance.setCurrentCycleCredit(new BigDecimal("2000.00"));
        balance.setCurrentCycleDebit(new BigDecimal("1000.00"));

        mockMvc.perform(put("/internal/accounts/1/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(balance)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(7500.00))
                .andExpect(jsonPath("$.currentCycleCredit").value(2000.00))
                .andExpect(jsonPath("$.currentCycleDebit").value(1000.00));
    }

    @Test
    void getAccount_notFound() throws Exception {
        mockMvc.perform(get("/api/accounts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCard_notFound() throws Exception {
        mockMvc.perform(get("/api/cards/0000000000000000"))
                .andExpect(status().isNotFound());
    }
}
