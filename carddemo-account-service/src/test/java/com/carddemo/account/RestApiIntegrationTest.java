package com.carddemo.account;

import com.carddemo.account.dto.BillingDto;
import com.carddemo.common.dto.AccountDto;
import com.carddemo.common.dto.CardDto;
import com.carddemo.common.dto.CardXrefDto;
import com.carddemo.common.dto.CustomerDto;
import com.carddemo.common.model.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class RestApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("carddemo_account_db")
            .withUsername("carddemo")
            .withPassword("carddemo");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    void listAccounts_paginated() {
        ResponseEntity<PagedResponse<AccountDto>> response = restTemplate.exchange(
                "/api/accounts?page=0&size=5",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSize()).isEqualTo(5);
    }

    @Test
    void getAccount_andUpdate() {
        ResponseEntity<PagedResponse<AccountDto>> listResponse = restTemplate.exchange(
                "/api/accounts?page=0&size=1",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().getContent()).isNotEmpty();

        AccountDto first = listResponse.getBody().getContent().get(0);
        Long acctId = first.getAcctId();

        ResponseEntity<AccountDto> getResponse = restTemplate.getForEntity(
                "/api/accounts/" + acctId, AccountDto.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getAcctId()).isEqualTo(acctId);

        AccountDto update = getResponse.getBody();
        update.setAcctAddrZip("99999");
        ResponseEntity<AccountDto> putResponse = restTemplate.exchange(
                "/api/accounts/" + acctId,
                HttpMethod.PUT,
                new HttpEntity<>(update),
                AccountDto.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isNotNull();
        assertThat(putResponse.getBody().getAcctAddrZip()).isEqualTo("99999");

        ResponseEntity<AccountDto> verifyResponse = restTemplate.getForEntity(
                "/api/accounts/" + acctId, AccountDto.class);
        assertThat(verifyResponse.getBody()).isNotNull();
        assertThat(verifyResponse.getBody().getAcctAddrZip()).isEqualTo("99999");
    }

    @Test
    void getAccount_notFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/accounts/999999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void listCards_paginated() {
        ResponseEntity<PagedResponse<CardDto>> response = restTemplate.exchange(
                "/api/cards?page=0&size=5",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSize()).isEqualTo(5);
    }

    @Test
    void cardSelectAndUpdate() {
        ResponseEntity<PagedResponse<CardDto>> listResponse = restTemplate.exchange(
                "/api/cards?page=0&size=1",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().getContent()).isNotEmpty();

        CardDto first = listResponse.getBody().getContent().get(0);
        String cardNum = first.getCardNum();

        ResponseEntity<CardDto> getResponse = restTemplate.getForEntity(
                "/api/cards/" + cardNum, CardDto.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        CardDto update = getResponse.getBody();
        assertThat(update).isNotNull();
        update.setCardEmbossedName("UPDATED NAME");
        ResponseEntity<CardDto> putResponse = restTemplate.exchange(
                "/api/cards/" + cardNum,
                HttpMethod.PUT,
                new HttpEntity<>(update),
                CardDto.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isNotNull();
        assertThat(putResponse.getBody().getCardEmbossedName()).isEqualTo("UPDATED NAME");
    }

    @Test
    void cardXrefLookup() {
        ResponseEntity<PagedResponse<CardDto>> listResponse = restTemplate.exchange(
                "/api/cards?page=0&size=1",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().getContent()).isNotEmpty();

        String cardNum = listResponse.getBody().getContent().get(0).getCardNum();

        ResponseEntity<CardXrefDto> xrefResponse = restTemplate.getForEntity(
                "/api/card-xref/" + cardNum, CardXrefDto.class);
        assertThat(xrefResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(xrefResponse.getBody()).isNotNull();
        assertThat(xrefResponse.getBody().getXrefCardNum()).isEqualTo(cardNum);
    }

    @Test
    void billingEndpoint() {
        ResponseEntity<PagedResponse<AccountDto>> listResponse = restTemplate.exchange(
                "/api/accounts?page=0&size=1",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().getContent()).isNotEmpty();

        Long acctId = listResponse.getBody().getContent().get(0).getAcctId();

        ResponseEntity<BillingDto> billingResponse = restTemplate.getForEntity(
                "/api/accounts/" + acctId + "/billing", BillingDto.class);
        assertThat(billingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(billingResponse.getBody()).isNotNull();
        assertThat(billingResponse.getBody().acctId()).isEqualTo(acctId);

        BigDecimal expected = billingResponse.getBody().creditLimit()
                .subtract(billingResponse.getBody().currentBalance());
        assertThat(billingResponse.getBody().availableCredit()).isEqualByComparingTo(expected);
    }

    @Test
    void listCustomers_paginated() {
        ResponseEntity<PagedResponse<CustomerDto>> response = restTemplate.exchange(
                "/api/customers?page=0&size=5",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSize()).isEqualTo(5);
    }

    @Test
    void getCustomer() {
        ResponseEntity<PagedResponse<CustomerDto>> listResponse = restTemplate.exchange(
                "/api/customers?page=0&size=1",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().getContent()).isNotEmpty();

        Long custId = listResponse.getBody().getContent().get(0).getCustId();

        ResponseEntity<CustomerDto> getResponse = restTemplate.getForEntity(
                "/api/customers/" + custId, CustomerDto.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getCustId()).isEqualTo(custId);
    }

    @Test
    void pagination_secondPage() {
        ResponseEntity<PagedResponse<AccountDto>> response = restTemplate.exchange(
                "/api/accounts?page=1&size=2",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPage()).isEqualTo(1);
    }
}
