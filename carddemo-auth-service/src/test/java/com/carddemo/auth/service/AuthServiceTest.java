package com.carddemo.auth.service;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.dto.RefreshTokenRequest;
import com.carddemo.auth.entity.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.auth.security.JwtTokenProvider;
import com.carddemo.common.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void login_withValidCredentials_shouldReturnTokens() {
        User user = User.builder()
                .userId("user01")
                .password("hashedPassword")
                .build();

        when(userRepository.findByUserId("user01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken("user01")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("user01")).thenReturn("refresh-token");

        LoginRequest request = LoginRequest.builder()
                .userId("user01")
                .password("password")
                .build();

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo("user01");
    }

    @Test
    void login_withUserNotFound_shouldThrowException() {
        when(userRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        LoginRequest request = LoginRequest.builder()
                .userId("unknown")
                .password("password")
                .build();

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void login_withWrongPassword_shouldThrowException() {
        User user = User.builder()
                .userId("user01")
                .password("hashedPassword")
                .build();

        when(userRepository.findByUserId("user01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashedPassword")).thenReturn(false);

        LoginRequest request = LoginRequest.builder()
                .userId("user01")
                .password("wrong")
                .build();

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void refresh_withValidToken_shouldReturnNewAccessToken() {
        when(jwtTokenProvider.validateToken("valid-refresh")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("valid-refresh")).thenReturn("user01");
        when(jwtTokenProvider.generateAccessToken("user01")).thenReturn("new-access-token");

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh")
                .build();

        LoginResponse response = authService.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh");
        assertThat(response.getUserId()).isEqualTo("user01");
    }

    @Test
    void refresh_withInvalidToken_shouldThrowException() {
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalid-token")
                .build();

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid refresh token");
    }
}
