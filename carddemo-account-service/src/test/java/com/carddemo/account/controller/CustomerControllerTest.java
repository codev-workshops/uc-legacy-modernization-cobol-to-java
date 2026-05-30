package com.carddemo.account.controller;

import com.carddemo.account.exception.GlobalExceptionHandler;
import com.carddemo.account.service.CustomerService;
import com.carddemo.common.dto.CustomerDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.model.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Test
    void listCustomers() throws Exception {
        CustomerDto dto = CustomerDto.builder().custId(100L).custFirstName("John").build();
        PagedResponse<CustomerDto> response = PagedResponse.<CustomerDto>builder()
                .content(List.of(dto)).page(0).size(20).totalElements(1).totalPages(1).build();
        when(customerService.listCustomers(0, 20)).thenReturn(response);

        mockMvc.perform(get("/api/customers").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].custId").value(100));
    }

    @Test
    void getCustomer_found() throws Exception {
        CustomerDto dto = CustomerDto.builder().custId(100L).custFirstName("John").build();
        when(customerService.getCustomer(100L)).thenReturn(dto);

        mockMvc.perform(get("/api/customers/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custId").value(100));
    }

    @Test
    void getCustomer_notFound() throws Exception {
        when(customerService.getCustomer(999L))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());
    }
}
