package com.carddemo.online;

import com.carddemo.common.entity.User;
import com.carddemo.common.repository.UserRepository;
import com.carddemo.online.dto.LoginRequest;
import com.carddemo.online.dto.LoginResponse;
import com.carddemo.online.dto.ReportRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportMenuIntegrationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

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

        adminToken = login("ADMIN01", "adminpwd");
        userToken = login("USER01", "userpwd");
    }

    private String login(String userId, String password) throws Exception {
        LoginRequest loginReq = new LoginRequest(userId, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResp = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);
        return loginResp.getToken();
    }

    @Test
    void menu_regularUser_returns11Items() throws Exception {
        mockMvc.perform(get("/api/menu")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11))
                .andExpect(jsonPath("$[0].name").value("Account View"))
                .andExpect(jsonPath("$[8].name").value("Transaction Reports"));
    }

    @Test
    void menu_adminUser_returns17Items() throws Exception {
        mockMvc.perform(get("/api/menu")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(17))
                .andExpect(jsonPath("$[11].name").value("User List (Security)"));
    }

    @Test
    void menu_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void generateReport_authenticated_returns202() throws Exception {
        ReportRequest request = new ReportRequest("2024-01-01", "2024-01-31");

        mockMvc.perform(post("/api/reports/generate")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.executionId").isNumber())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.outputPath").exists());
    }

    @Test
    void generateReport_thenGetReport_returnsStatus() throws Exception {
        ReportRequest request = new ReportRequest("2024-01-01", "2024-01-31");

        MvcResult generateResult = mockMvc.perform(post("/api/reports/generate")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andReturn();

        Long executionId = objectMapper.readTree(
                generateResult.getResponse().getContentAsString())
                .get("executionId").asLong();

        // Wait briefly for async job
        Thread.sleep(500);

        mockMvc.perform(get("/api/reports/" + executionId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(executionId))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void getReport_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/reports/99999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateReport_invalidDates_returns400() throws Exception {
        ReportRequest request = new ReportRequest("bad", "dates");

        mockMvc.perform(post("/api/reports/generate")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
