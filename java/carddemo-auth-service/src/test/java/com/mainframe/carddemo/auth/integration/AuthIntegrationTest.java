package com.mainframe.carddemo.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainframe.carddemo.auth.dto.LoginRequest;
import com.mainframe.carddemo.auth.dto.UpdateUserRequest;
import com.mainframe.carddemo.auth.dto.UserRequest;
import com.mainframe.carddemo.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void login_validAdmin_returnsJwtWithAdminType() throws Exception {
        LoginRequest request = new LoginRequest("admin01", "password");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").value("admin01"))
                .andExpect(jsonPath("$.userType").value("ADMIN"))
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo("admin01");
        assertThat(jwtTokenProvider.getRole(token)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void login_validUser_returnsJwtWithUserType() throws Exception {
        LoginRequest request = new LoginRequest("user0001", "password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("USER"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest request = new LoginRequest("admin01", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_userNotFound_returns401() throws Exception {
        LoginRequest request = new LoginRequest("nouser", "password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCrud_fullLifecycle() throws Exception {
        String adminToken = "Bearer " + jwtTokenProvider.generateToken("admin01", "A");

        // Create user
        UserRequest createReq = new UserRequest("testcrd", "Create", "Test", "secret", "U");
        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("testcrd"));

        // List users
        mockMvc.perform(get("/api/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // Update user
        UpdateUserRequest updateReq = new UpdateUserRequest();
        updateReq.setFirstName("Updated");
        mockMvc.perform(put("/api/users/testcrd")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));

        // Delete user
        mockMvc.perform(delete("/api/users/testcrd")
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(put("/api/users/testcrd")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userEndpoints_asRegularUser_returns403() throws Exception {
        String userToken = "Bearer " + jwtTokenProvider.generateToken("user0001", "U");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/users")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserRequest("hacker", "H", "K", "pw", "A"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/users/admin01")
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
