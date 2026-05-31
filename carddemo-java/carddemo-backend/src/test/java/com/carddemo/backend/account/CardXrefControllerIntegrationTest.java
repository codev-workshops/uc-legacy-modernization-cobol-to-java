package com.carddemo.backend.account;

import com.carddemo.common.dto.CardXrefDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class CardXrefControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getXrefByCardNum_seededData() {
        ResponseEntity<CardXrefDto> response = restTemplate.getForEntity(
                "/api/v1/xref/card/0500024453765740", CardXrefDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CardXrefDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals("0500024453765740", dto.getCardNumber());
        assertEquals(50L, dto.getCustomerId());
        assertEquals(50L, dto.getAccountId());
    }

    @Test
    void getXrefByCardNum_notFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/xref/card/9999999999999999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getXrefByAcctId_seededData() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/xref/account/50", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("0500024453765740"));
    }

    @Test
    void getXrefByAcctId_empty() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/xref/account/999999", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("[]", response.getBody());
    }
}
