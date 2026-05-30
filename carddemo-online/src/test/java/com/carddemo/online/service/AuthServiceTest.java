package com.carddemo.online.service;

import com.carddemo.common.entity.User;
import com.carddemo.common.repository.UserRepository;
import com.carddemo.online.dto.LoginRequest;
import com.carddemo.online.dto.LoginResponse;
import com.carddemo.online.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtUtil);
    }

    @Test
    void login_validCredentials_returnsToken() {
        User user = new User();
        user.setUsrId("ADMIN01");
        user.setPwd("secret");
        user.setUsrType("A");

        when(userRepository.findById("ADMIN01")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("ADMIN01", "A")).thenReturn("test-token");

        LoginResponse response = authService.login(new LoginRequest("ADMIN01", "secret"));

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals("ADMIN01", response.getUserId());
        assertEquals("A", response.getUserType());
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userRepository.findById("NOBODY")).thenReturn(Optional.empty());

        assertThrows(AuthService.AuthenticationException.class,
                () -> authService.login(new LoginRequest("NOBODY", "pwd")));
    }

    @Test
    void login_wrongPassword_throwsException() {
        User user = new User();
        user.setUsrId("ADMIN01");
        user.setPwd("correct");
        user.setUsrType("A");

        when(userRepository.findById("ADMIN01")).thenReturn(Optional.of(user));

        assertThrows(AuthService.AuthenticationException.class,
                () -> authService.login(new LoginRequest("ADMIN01", "wrong")));
    }
}
