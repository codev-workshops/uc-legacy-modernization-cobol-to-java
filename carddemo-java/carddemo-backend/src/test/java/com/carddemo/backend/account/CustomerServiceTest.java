package com.carddemo.backend.account;

import com.carddemo.backend.account.entity.CustomerEntity;
import com.carddemo.backend.account.repository.CustomerRepository;
import com.carddemo.backend.account.service.CustomerService;
import com.carddemo.common.dto.CustomerDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerEntity sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = new CustomerEntity();
        sampleCustomer.setCustId(2L);
        sampleCustomer.setFirstName("Enrico");
        sampleCustomer.setMiddleName("April");
        sampleCustomer.setLastName("Rosenbaum");
        sampleCustomer.setAddrLine1("4917 Myrna Flats");
        sampleCustomer.setAddrLine2("Apt. 453");
        sampleCustomer.setAddrLine3("West Bernita");
        sampleCustomer.setStateCd("IN");
        sampleCustomer.setCountryCd("USA");
        sampleCustomer.setZip("22770");
        sampleCustomer.setPhone1("(429)706-9510");
        sampleCustomer.setPhone2("(744)950-5272");
        sampleCustomer.setSsn(587518382L);
        sampleCustomer.setGovtIssuedId("00000000000506210371");
        sampleCustomer.setDob("1961-10-08");
        sampleCustomer.setEftAccountId("0069194009");
        sampleCustomer.setPriCardHolderInd("Y");
        sampleCustomer.setFicoCreditScore(268);
    }

    @Test
    void findById_returnsDto() {
        when(customerRepository.findById(2L)).thenReturn(Optional.of(sampleCustomer));

        CustomerDto result = customerService.findById(2L);

        assertEquals(2L, result.getCustomerId());
        assertEquals("Enrico", result.getFirstName());
        assertEquals("April", result.getMiddleName());
        assertEquals("Rosenbaum", result.getLastName());
        assertEquals("4917 Myrna Flats", result.getAddressLine1());
        assertEquals("Apt. 453", result.getAddressLine2());
        assertEquals("IN", result.getState());
        assertEquals("22770", result.getZipCode());
        assertEquals("(429)706-9510", result.getPhone());
        assertEquals("587518382", result.getSsn());
        assertEquals("268", result.getFico());
        assertEquals("00000000000506210371", result.getGovtIssuedId());
        assertEquals("1961-10-08", result.getDateOfBirth());
        assertEquals("USA", result.getCountryCd());
        assertEquals("(744)950-5272", result.getPhone2());
        assertEquals("0069194009", result.getEftAccountId());
        assertEquals("Y", result.getPriCardHolderInd());
    }

    @Test
    void findById_notFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> customerService.findById(999L));
    }

    @Test
    void findAll_paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerEntity> page = new PageImpl<>(List.of(sampleCustomer), pageable, 1);
        when(customerRepository.findAll(pageable)).thenReturn(page);

        Page<CustomerDto> result = customerService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(2L, result.getContent().get(0).getCustomerId());
        assertEquals("Enrico", result.getContent().get(0).getFirstName());
    }
}
