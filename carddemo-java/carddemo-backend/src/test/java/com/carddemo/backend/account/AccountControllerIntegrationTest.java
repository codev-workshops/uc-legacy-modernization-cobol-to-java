package com.carddemo.backend.account;

import com.carddemo.common.dto.AccountDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getAccount_seededData() {
        ResponseEntity<AccountDto> response = restTemplate.getForEntity(
                "/api/v1/accounts/2", AccountDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals(2L, dto.getAccountId());
        assertEquals("Y", dto.getAccountStatus());
        assertNotNull(dto.getCurrentBalance());
    }

    @Test
    void getAccount_notFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/accounts/99999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listAccounts_paginated() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/accounts?page=0&size=5", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("content"));
    }

    @Test
    void updateAccount_success() {
        AccountDto update = new AccountDto();
        update.setCreditLimit(new BigDecimal("5000.00"));
        update.setCashCreditLimit(new BigDecimal("2500.00"));

        ResponseEntity<AccountDto> response = restTemplate.exchange(
                "/api/v1/accounts/2", HttpMethod.PUT,
                new HttpEntity<>(update), AccountDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(new BigDecimal("5000.00"), response.getBody().getCreditLimit());
    }

    @Test
    void exportAccounts() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/accounts/export", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
