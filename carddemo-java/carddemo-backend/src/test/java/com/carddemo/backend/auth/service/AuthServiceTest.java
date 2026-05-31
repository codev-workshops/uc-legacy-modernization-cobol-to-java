package com.carddemo.backend.auth.service;

import com.carddemo.backend.auth.dto.LoginRequest;
import com.carddemo.backend.auth.dto.LoginResponse;
import com.carddemo.backend.auth.entity.UserEntity;
import com.carddemo.backend.auth.repository.UserRepository;
import com.carddemo.backend.auth.security.JwtTokenProvider;
import com.carddemo.common.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUserId("USER0001");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setUserType("U");
    }

    @Test
    void authenticate_validCredentials_returnsLoginResponse() {
        LoginRequest request = new LoginRequest("USER0001", "password123");

        when(userRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("USER0001", "U")).thenReturn("jwt-token");

        LoginResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("USER0001", response.getUserId());
        assertEquals("U", response.getUserType());
    }

    @Test
    void authenticate_wrongPassword_throwsAuthenticationException() {
        LoginRequest request = new LoginRequest("USER0001", "wrongpassword");

        when(userRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedpassword")).thenReturn(false);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.authenticate(request));
        assertEquals("Invalid user ID or password", ex.getMessage());
    }

    @Test
    void authenticate_userNotFound_throwsAuthenticationException() {
        LoginRequest request = new LoginRequest("INVALID", "password123");

        when(userRepository.findById("INVALID")).thenReturn(Optional.empty());

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.authenticate(request));
        assertEquals("Invalid user ID or password", ex.getMessage());
    }
}
