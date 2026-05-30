package com.mainframe.carddemo.account.service;

import com.mainframe.carddemo.account.entity.Customer;
import com.mainframe.carddemo.account.repository.CustomerRepository;
import com.mainframe.carddemo.common.dto.CustomerDto;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        return toDto(customer);
    }

    public static CustomerDto toDto(Customer c) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(c.getCustId());
        dto.setFirstName(c.getCustFirstName());
        dto.setMiddleName(c.getCustMiddleName());
        dto.setLastName(c.getCustLastName());
        dto.setAddressLine1(c.getCustAddrLine1());
        dto.setAddressLine2(c.getCustAddrLine2());
        dto.setAddressLine3(c.getCustAddrLine3());
        dto.setStateCode(c.getCustAddrStateCd());
        dto.setCountryCode(c.getCustAddrCountryCd());
        dto.setZip(c.getCustAddrZip());
        dto.setPhoneNum1(c.getCustPhoneNum1());
        dto.setPhoneNum2(c.getCustPhoneNum2());
        dto.setSsn(c.getCustSsn());
        dto.setGovtIssuedId(c.getCustGovtIssuedId());
        dto.setDob(c.getCustDob());
        dto.setEftAccountId(c.getCustEftAccountId());
        dto.setPrimaryCardHolderInd(c.getCustPriCardHolderInd());
        dto.setFicoCreditScore(c.getCustFicoCreditScore());
        return dto;
    }
}
