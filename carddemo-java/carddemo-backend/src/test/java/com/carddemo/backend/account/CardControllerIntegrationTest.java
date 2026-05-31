package com.carddemo.backend.account;

import com.carddemo.common.dto.CardDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class CardControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getCard_seededData() {
        ResponseEntity<CardDto> response = restTemplate.getForEntity(
                "/api/v1/cards/0500024453765740", CardDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CardDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals("0500024453765740", dto.getCardNumber());
        assertEquals(50L, dto.getAccountId());
    }

    @Test
    void listCards_paginated() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/cards?page=0&size=5", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("content"));
    }

    @Test
    void listCards_filteredByAcctId() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/cards?acctId=50&page=0&size=10", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getCard_notFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/cards/9999999999999999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateCard_success() {
        CardDto update = new CardDto();
        update.setCardStatus("N");

        ResponseEntity<CardDto> response = restTemplate.exchange(
                "/api/v1/cards/0500024453765740", HttpMethod.PUT,
                new HttpEntity<>(update), CardDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void exportCards() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/cards/export", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
