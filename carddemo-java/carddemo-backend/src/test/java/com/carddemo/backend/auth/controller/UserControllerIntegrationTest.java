package com.carddemo.backend.auth.controller;

import com.carddemo.backend.auth.dto.CreateUserRequest;
import com.carddemo.backend.auth.dto.LoginRequest;
import com.carddemo.backend.auth.dto.LoginResponse;
import com.carddemo.backend.auth.dto.UpdateUserRequest;
import com.carddemo.backend.auth.entity.UserEntity;
import com.carddemo.backend.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("carddemo_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.profiles.active", () -> "integration");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        UserEntity admin = new UserEntity();
        admin.setUserId("ADMIN001");
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setUserType("A");
        userRepository.save(admin);

        UserEntity user = new UserEntity();
        user.setUserId("USER0001");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPasswordHash(passwordEncoder.encode("user1234"));
        user.setUserType("U");
        userRepository.save(user);

        adminToken = loginAndGetToken("ADMIN001", "admin123");
    }

    @Test
    void listUsers_asAdmin_returnsUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void createUser_asAdmin_createsUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserId("NEWUSR01");
        request.setFirstName("New");
        request.setLastName("User");
        request.setPassword("password123");
        request.setUserType("U");

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("NEWUSR01"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.userType").value("U"));
    }

    @Test
    void createUser_duplicateId_returns409() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserId("USER0001");
        request.setFirstName("Dup");
        request.setLastName("User");
        request.setPassword("password123");
        request.setUserType("U");

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_asAdmin_updatesUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");

        mockMvc.perform(put("/api/v1/users/USER0001")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
    }

    @Test
    void deleteUser_asAdmin_deletesUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/USER0001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/users/INVALID")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void userEndpoints_asNonAdmin_returns403() throws Exception {
        String userToken = loginAndGetToken("USER0001", "user1234");

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullCrudLifecycle_asAdmin() throws Exception {
        // Create
        CreateUserRequest createReq = new CreateUserRequest();
        createReq.setUserId("LCYCL01");
        createReq.setFirstName("Lifecycle");
        createReq.setLastName("Test");
        createReq.setPassword("password123");
        createReq.setUserType("U");

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("LCYCL01"));

        // Read
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));

        // Update
        UpdateUserRequest updateReq = new UpdateUserRequest();
        updateReq.setFirstName("Updated");

        mockMvc.perform(put("/api/v1/users/LCYCL01")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));

        // Delete
        mockMvc.perform(delete("/api/v1/users/LCYCL01")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    private String loginAndGetToken(String userId, String password) throws Exception {
        LoginRequest request = new LoginRequest(userId, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);
        return response.getToken();
    }
}
