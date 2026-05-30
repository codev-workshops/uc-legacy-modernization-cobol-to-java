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
import com.carddemo.online.dto.CardUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CardIntegrationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        cardXrefRepository.deleteAll();
        cardRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();

        User admin = new User();
        admin.setUsrId("ADMIN01");
        admin.setFname("Admin");
        admin.setLname("User");
        admin.setPwd("adminpwd");
        admin.setUsrType("A");
        userRepository.save(admin);

        User regular = new User();
        regular.setUsrId("USER01");
        regular.setFname("Regular");
        regular.setLname("User");
        regular.setPwd("userpwd");
        regular.setUsrType("U");
        userRepository.save(regular);

        Account account = new Account();
        account.setAcctId(1001L);
        account.setActiveStatus("Y");
        account.setCurrBal(new BigDecimal("5000.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setExpirationDate("2026-12-31");
        accountRepository.save(account);

        Customer customer = new Customer();
        customer.setCustId(5001L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customerRepository.save(customer);

        Card card1 = new Card();
        card1.setCardNum("4111111111111111");
        card1.setAcctId(1001L);
        card1.setCvvCd(123);
        card1.setEmbossedName("JOHN DOE");
        card1.setExpirationDate("2025-12-31");
        card1.setActiveStatus("Y");
        cardRepository.save(card1);

        Card card2 = new Card();
        card2.setCardNum("5222222222222222");
        card2.setAcctId(1001L);
        card2.setCvvCd(456);
        card2.setEmbossedName("JANE DOE");
        card2.setExpirationDate("2026-06-30");
        card2.setActiveStatus("Y");
        cardRepository.save(card2);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setAcctId(1001L);
        xref.setCustId(5001L);
        cardXrefRepository.save(xref);

        adminToken = login("ADMIN01", "adminpwd");
        userToken = login("USER01", "userpwd");
    }

    private String login(String userId, String password) throws Exception {
        LoginRequest loginReq = new LoginRequest(userId, password);
        MvcResult result = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();
        LoginResponse loginResp = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);
        return loginResp.getToken();
    }

    @Test
    void listCards_asAdmin_returnsAllPaginated() throws Exception {
        mockMvc.perform(get("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void listCards_asRegularUser_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void listCards_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listCards_pagination_secondPage() throws Exception {
        mockMvc.perform(get("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void getCard_withXref_returnsDetailWithAccountAndCustomer() throws Exception {
        mockMvc.perform(get("/api/cards/4111111111111111")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"))
                .andExpect(jsonPath("$.embossedName").value("JOHN DOE"))
                .andExpect(jsonPath("$.account.acctId").value(1001))
                .andExpect(jsonPath("$.account.currBal").value(5000.00))
                .andExpect(jsonPath("$.account.creditLimit").value(10000.00))
                .andExpect(jsonPath("$.customer.custId").value(5001))
                .andExpect(jsonPath("$.customer.firstName").value("John"))
                .andExpect(jsonPath("$.customer.lastName").value("Doe"));
    }

    @Test
    void getCard_withoutXref_returnsDetailWithoutAccountCustomer() throws Exception {
        mockMvc.perform(get("/api/cards/5222222222222222")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("5222222222222222"))
                .andExpect(jsonPath("$.embossedName").value("JANE DOE"))
                .andExpect(jsonPath("$.account").doesNotExist())
                .andExpect(jsonPath("$.customer").doesNotExist());
    }

    @Test
    void getCard_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/cards/9999999999999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Card not found: 9999999999999999"));
    }

    @Test
    void getCard_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/cards/4111111111111111"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCard_success() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("UPDATED NAME");
        request.setActiveStatus("N");

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("UPDATED NAME"))
                .andExpect(jsonPath("$.activeStatus").value("N"))
                .andExpect(jsonPath("$.cardNum").value("4111111111111111"));
    }

    @Test
    void updateCard_partialUpdate() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("PARTIAL UPDATE");

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("PARTIAL UPDATE"))
                .andExpect(jsonPath("$.activeStatus").value("Y"))
                .andExpect(jsonPath("$.acctId").value(1001));
    }

    @Test
    void updateCard_asRegularUser_success() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("USER UPDATE");

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("USER UPDATE"));
    }

    @Test
    void updateCard_notFound_returns404() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("NOBODY");

        mockMvc.perform(put("/api/cards/9999999999999999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_invalidActiveStatus_returns400() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setActiveStatus("X");

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCard_unauthenticated_returns401() throws Exception {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("TEST");

        mockMvc.perform(put("/api/cards/4111111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
