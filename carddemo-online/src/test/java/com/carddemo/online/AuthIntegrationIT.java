package com.carddemo.online;

import com.carddemo.common.entity.User;
import com.carddemo.common.repository.UserRepository;
import com.carddemo.online.dto.LoginRequest;
import com.carddemo.online.dto.LoginResponse;
import com.carddemo.online.dto.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        User admin = new User();
        admin.setUsrId("ADMIN01");
        admin.setFname("Admin");
        admin.setLname("User");
        admin.setPwd("adminpwd");
        admin.setUsrType("A");
        userRepository.save(admin);

        User regular = new User();
        regular.setUsrId("USER01");
        regular.setFname("Regular");
        regular.setLname("User");
        regular.setPwd("userpwd");
        regular.setUsrType("U");
        userRepository.save(regular);

        // Login as admin to get token
        LoginRequest loginReq = new LoginRequest("ADMIN01", "adminpwd");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResp = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);
        adminToken = loginResp.getToken();
    }

    @Test
    void fullAuthFlow_loginAndAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void regularUser_cannotAccessUserEndpoints() throws Exception {
        LoginRequest loginReq = new LoginRequest("USER01", "userpwd");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResp = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + loginResp.getToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticated_cannotAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_invalidCredentials() throws Exception {
        LoginRequest loginReq = new LoginRequest("ADMIN01", "wrongpwd");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_crudUserFlow() throws Exception {
        // Create user
        UserRequest createReq = new UserRequest();
        createReq.setUsrId("NEW01");
        createReq.setFname("New");
        createReq.setLname("User");
        createReq.setPwd("newpwd");
        createReq.setUsrType("U");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usrId").value("NEW01"));

        // Update user
        UserRequest updateReq = new UserRequest();
        updateReq.setUsrId("NEW01");
        updateReq.setFname("Updated");
        updateReq.setLname("User");
        updateReq.setPwd("newpwd2");
        updateReq.setUsrType("A");

        mockMvc.perform(put("/api/users/NEW01")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fname").value("Updated"))
                .andExpect(jsonPath("$.usrType").value("A"));

        // Delete user
        mockMvc.perform(delete("/api/users/NEW01")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void admin_createDuplicateUser_returns409() throws Exception {
        UserRequest createReq = new UserRequest();
        createReq.setUsrId("ADMIN01");
        createReq.setFname("Dup");
        createReq.setLname("User");
        createReq.setPwd("pwd");
        createReq.setUsrType("A");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isConflict());
    }

    @Test
    void admin_updateNonexistentUser_returns404() throws Exception {
        UserRequest updateReq = new UserRequest();
        updateReq.setUsrId("NOBODY");
        updateReq.setFname("X");
        updateReq.setLname("Y");
        updateReq.setPwd("pwd");
        updateReq.setUsrType("U");

        mockMvc.perform(put("/api/users/NOBODY")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    void admin_deleteNonexistentUser_returns404() throws Exception {
        mockMvc.perform(delete("/api/users/NOBODY")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }
}
