package com.carddemo.backend.auth.service;

import com.carddemo.backend.auth.dto.LoginRequest;
import com.carddemo.backend.auth.dto.LoginResponse;
import com.carddemo.backend.auth.entity.UserEntity;
import com.carddemo.backend.auth.repository.UserRepository;
import com.carddemo.backend.auth.security.JwtTokenProvider;
import com.carddemo.common.exception.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse authenticate(LoginRequest request) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AuthenticationException("Invalid user ID or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid user ID or password");
        }

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getUserType());
        return new LoginResponse(token, user.getUserId(), user.getUserType());
    }
}
