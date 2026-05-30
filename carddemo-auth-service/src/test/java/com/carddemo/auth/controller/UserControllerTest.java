package com.carddemo.auth.controller;

import com.carddemo.auth.exception.GlobalExceptionHandler;
import com.carddemo.auth.security.JwtAuthenticationFilter;
import com.carddemo.auth.security.JwtTokenProvider;
import com.carddemo.auth.security.SecurityConfig;
import com.carddemo.auth.service.UserService;
import com.carddemo.common.dto.UserDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser
    void listUsers_shouldReturn200() throws Exception {
        UserDto dto = UserDto.builder().userId("user01").firstName("Test").build();
        when(userService.listUsers(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value("user01"));
    }

    @Test
    void listUsers_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getUser_shouldReturn200() throws Exception {
        UserDto dto = UserDto.builder().userId("user01").firstName("Test").build();
        when(userService.getUser("user01")).thenReturn(dto);

        mockMvc.perform(get("/api/users/user01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user01"));
    }

    @Test
    @WithMockUser
    void getUser_notFound_shouldReturn404() throws Exception {
        when(userService.getUser("unknown"))
                .thenThrow(new ResourceNotFoundException("User not found: unknown"));

        mockMvc.perform(get("/api/users/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: unknown"));
    }

    @Test
    @WithMockUser
    void createUser_shouldReturn201() throws Exception {
        UserDto input = UserDto.builder()
                .userId("new01")
                .firstName("New")
                .lastName("User")
                .password("pass")
                .userType("U")
                .build();
        UserDto output = UserDto.builder()
                .userId("new01")
                .firstName("New")
                .lastName("User")
                .userType("U")
                .build();

        when(userService.createUser(any(UserDto.class))).thenReturn(output);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("new01"));
    }

    @Test
    @WithMockUser
    void updateUser_shouldReturn200() throws Exception {
        UserDto input = UserDto.builder()
                .userId("user01")
                .firstName("Updated")
                .build();
        UserDto output = UserDto.builder()
                .userId("user01")
                .firstName("Updated")
                .build();

        when(userService.updateUser(eq("user01"), any(UserDto.class))).thenReturn(output);

        mockMvc.perform(put("/api/users/user01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    @WithMockUser
    void deleteUser_shouldReturn204() throws Exception {
        doNothing().when(userService).deleteUser("user01");

        mockMvc.perform(delete("/api/users/user01"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteUser_notFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("User not found: unknown"))
                .when(userService).deleteUser("unknown");

        mockMvc.perform(delete("/api/users/unknown"))
                .andExpect(status().isNotFound());
    }
}
