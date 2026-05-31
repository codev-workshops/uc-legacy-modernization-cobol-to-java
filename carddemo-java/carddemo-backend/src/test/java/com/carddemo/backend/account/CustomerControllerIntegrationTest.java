package com.carddemo.backend.account;

import com.carddemo.common.dto.CustomerDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class CustomerControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getCustomer_seededData() {
        ResponseEntity<CustomerDto> response = restTemplate.getForEntity(
                "/api/v1/customers/2", CustomerDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CustomerDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals(2L, dto.getCustomerId());
        assertEquals("Enrico", dto.getFirstName());
        assertEquals("Rosenbaum", dto.getLastName());
    }

    @Test
    void getCustomer_notFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/customers/99999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listCustomers_paginated() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/customers?page=0&size=5", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("content"));
    }
}
