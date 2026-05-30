package com.mainframe.carddemo.account.controller;

import com.mainframe.carddemo.account.service.CustomerService;
import com.mainframe.carddemo.account.service.ResourceNotFoundException;
import com.mainframe.carddemo.common.dto.CustomerDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    void getCustomer_shouldReturnCustomer() throws Exception {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(100L);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setZip("90210");
        when(customerService.getCustomerById(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/customers/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(100))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getCustomer_notFound_shouldReturn404() throws Exception {
        when(customerService.getCustomerById(999L))
                .thenThrow(new ResourceNotFoundException("Customer not found: 999"));

        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());
    }
}
