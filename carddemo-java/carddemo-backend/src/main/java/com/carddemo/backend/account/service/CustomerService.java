package com.carddemo.backend.account.service;

import com.carddemo.backend.account.entity.CustomerEntity;
import com.carddemo.backend.account.repository.CustomerRepository;
import com.carddemo.common.dto.CustomerDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerDto findById(Long id) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "custId", id));
        return toDto(entity);
    }

    public Page<CustomerDto> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toDto);
    }

    private CustomerDto toDto(CustomerEntity entity) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(entity.getCustId());
        dto.setFirstName(entity.getFirstName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setLastName(entity.getLastName());
        dto.setAddressLine1(entity.getAddrLine1());
        dto.setAddressLine2(entity.getAddrLine2());
        dto.setCity(entity.getAddrLine3());
        dto.setState(entity.getStateCd());
        dto.setZipCode(entity.getZip());
        dto.setPhone(entity.getPhone1());
        dto.setSsn(entity.getSsn() != null ? entity.getSsn().toString() : null);
        dto.setFico(entity.getFicoCreditScore() != null ? entity.getFicoCreditScore().toString() : null);
        dto.setGovtIssuedId(entity.getGovtIssuedId());
        dto.setDateOfBirth(entity.getDob());
        dto.setCountryCd(entity.getCountryCd());
        dto.setPhone2(entity.getPhone2());
        dto.setEftAccountId(entity.getEftAccountId());
        dto.setPriCardHolderInd(entity.getPriCardHolderInd());
        dto.setAddressLine3(entity.getAddrLine3());
        return dto;
    }
}
