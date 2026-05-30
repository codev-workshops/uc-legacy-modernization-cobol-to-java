package com.carddemo.account.service;

import com.carddemo.account.entity.Customer;
import com.carddemo.account.mapper.CustomerMapper;
import com.carddemo.account.repository.CustomerRepository;
import com.carddemo.common.dto.CustomerDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public CustomerDto getCustomer(Long custId) {
        log.debug("Fetching customer with ID: {}", custId);
        Customer customer = customerRepository.findById(custId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + custId));
        return customerMapper.toDto(customer);
    }

    public PagedResponse<CustomerDto> listCustomers(int page, int size) {
        log.debug("Listing customers - page: {}, size: {}", page, size);
        Page<Customer> customerPage = customerRepository.findAll(PageRequest.of(page, size));
        return PagedResponse.<CustomerDto>builder()
                .content(customerPage.getContent().stream().map(customerMapper::toDto).toList())
                .page(customerPage.getNumber())
                .size(customerPage.getSize())
                .totalElements(customerPage.getTotalElements())
                .totalPages(customerPage.getTotalPages())
                .build();
    }
}
