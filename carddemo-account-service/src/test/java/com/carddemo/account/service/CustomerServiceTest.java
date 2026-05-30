package com.carddemo.account.service;

import com.carddemo.account.entity.Customer;
import com.carddemo.account.mapper.CustomerMapper;
import com.carddemo.account.repository.CustomerRepository;
import com.carddemo.common.dto.CustomerDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerDto customerDto;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .custId(100L)
                .custFirstName("John")
                .custLastName("Doe")
                .custAddrStateCd("NY")
                .custAddrZip("10001")
                .build();

        customerDto = CustomerDto.builder()
                .custId(100L)
                .custFirstName("John")
                .custLastName("Doe")
                .custAddrStateCd("NY")
                .custAddrZip("10001")
                .build();
    }

    @Test
    void getCustomer_found() {
        when(customerRepository.findById(100L)).thenReturn(Optional.of(customer));
        when(customerMapper.toDto(customer)).thenReturn(customerDto);

        CustomerDto result = customerService.getCustomer(100L);

        assertThat(result.getCustId()).isEqualTo(100L);
        assertThat(result.getCustFirstName()).isEqualTo("John");
    }

    @Test
    void getCustomer_notFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomer(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    void listCustomers() {
        Page<Customer> page = new PageImpl<>(List.of(customer), PageRequest.of(0, 20), 1);
        when(customerRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);
        when(customerMapper.toDto(customer)).thenReturn(customerDto);

        PagedResponse<CustomerDto> result = customerService.listCustomers(0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
