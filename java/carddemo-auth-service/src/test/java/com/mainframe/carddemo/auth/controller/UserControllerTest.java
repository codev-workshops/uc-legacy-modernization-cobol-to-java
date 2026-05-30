package com.mainframe.carddemo.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainframe.carddemo.auth.dto.UpdateUserRequest;
import com.mainframe.carddemo.auth.dto.UserRequest;
import com.mainframe.carddemo.auth.dto.UserResponse;
import com.mainframe.carddemo.auth.security.JwtAuthenticationFilter;
import com.mainframe.carddemo.auth.security.JwtTokenProvider;
import com.mainframe.carddemo.auth.security.SecurityConfig;
import com.mainframe.carddemo.auth.service.UserAlreadyExistsException;
import com.mainframe.carddemo.auth.service.UserNotFoundException;
import com.mainframe.carddemo.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserService userService;

    private String adminToken() {
        return "Bearer " + jwtTokenProvider.generateToken("admin01", "A");
    }

    private String userToken() {
        return "Bearer " + jwtTokenProvider.generateToken("user0001", "U");
    }

    @Test
    void listUsers_asAdmin_returnsPage() throws Exception {
        UserResponse user = new UserResponse("user0001", "Test", "User", "USER");
        when(userService.listUsers(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value("user0001"));
    }

    @Test
    void listUsers_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", userToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createUser_asAdmin_returns201() throws Exception {
        UserRequest request = new UserRequest("newuser", "New", "User", "pass1234", "U");
        UserResponse response = new UserResponse("newuser", "New", "User", "USER");
        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("newuser"));
    }

    @Test
    void createUser_duplicate_returns409() throws Exception {
        UserRequest request = new UserRequest("admin01", "Sys", "Admin", "pass", "A");
        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists: admin01"));

        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_asAdmin_returns200() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        UserResponse response = new UserResponse("user0001", "Updated", "User", "USER");
        when(userService.updateUser(eq("user0001"), any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/user0001")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void updateUser_notFound_returns404() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        when(userService.updateUser(eq("nouser"), any(UpdateUserRequest.class)))
                .thenThrow(new UserNotFoundException("User not found: nouser"));

        mockMvc.perform(put("/api/users/nouser")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_asAdmin_returns204() throws Exception {
        doNothing().when(userService).deleteUser("user0001");

        mockMvc.perform(delete("/api/users/user0001")
                        .header("Authorization", adminToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        doThrow(new UserNotFoundException("User not found: nouser"))
                .when(userService).deleteUser("nouser");

        mockMvc.perform(delete("/api/users/nouser")
                        .header("Authorization", adminToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_asUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/users/user0001")
                        .header("Authorization", userToken()))
                .andExpect(status().isForbidden());
    }
}
