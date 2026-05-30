package com.carddemo.online.controller;

import com.carddemo.online.config.SecurityConfig;
import com.carddemo.online.dto.UserRequest;
import com.carddemo.online.dto.UserResponse;
import com.carddemo.online.security.JwtAuthenticationFilter;
import com.carddemo.online.security.JwtUtil;
import com.carddemo.online.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(controllers = UserController.class,
        excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsers_asAdmin_returnsOk() throws Exception {
        UserResponse user = new UserResponse("USER01", "John", "Doe", "U");
        when(userService.listUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usrId").value("USER01"))
                .andExpect(jsonPath("$[0].fname").value("John"))
                .andExpect(jsonPath("$[0].usrType").value("U"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void listUsers_asRegularUser_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUser_asAdmin_returns201() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsrId("NEWUSR");
        request.setFname("Jane");
        request.setLname("Doe");
        request.setPwd("pass123");
        request.setUsrType("U");

        UserResponse response = new UserResponse("NEWUSR", "Jane", "Doe", "U");
        when(userService.addUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usrId").value("NEWUSR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUser_duplicate_returns409() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsrId("DUP");
        request.setFname("Jane");
        request.setLname("Doe");
        request.setPwd("pass123");
        request.setUsrType("U");

        when(userService.addUser(any(UserRequest.class)))
                .thenThrow(new UserService.UserAlreadyExistsException("User already exists: DUP"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_asAdmin_returnsOk() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsrId("USER01");
        request.setFname("Updated");
        request.setLname("Name");
        request.setPwd("newpwd");
        request.setUsrType("A");

        UserResponse response = new UserResponse("USER01", "Updated", "Name", "A");
        when(userService.updateUser(eq("USER01"), any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/USER01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fname").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_notFound_returns404() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsrId("NOPE");
        request.setFname("X");
        request.setLname("Y");
        request.setPwd("pwd");
        request.setUsrType("U");

        when(userService.updateUser(eq("NOPE"), any(UserRequest.class)))
                .thenThrow(new UserService.UserNotFoundException("User not found: NOPE"));

        mockMvc.perform(put("/api/users/NOPE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_asAdmin_returns204() throws Exception {
        doNothing().when(userService).deleteUser("USER01");

        mockMvc.perform(delete("/api/users/USER01"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_notFound_returns404() throws Exception {
        doThrow(new UserService.UserNotFoundException("User not found: NOPE"))
                .when(userService).deleteUser("NOPE");

        mockMvc.perform(delete("/api/users/NOPE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUser_invalidType_returns400() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsrId("USR01");
        request.setFname("Jane");
        request.setLname("Doe");
        request.setPwd("pass123");
        request.setUsrType("X");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addUser_asRegularUser_returns403() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsrId("USR01");
        request.setFname("Jane");
        request.setLname("Doe");
        request.setPwd("pass123");
        request.setUsrType("U");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
