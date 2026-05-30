package com.carddemo.online.service;

import com.carddemo.common.entity.User;
import com.carddemo.common.repository.UserRepository;
import com.carddemo.online.dto.LoginRequest;
import com.carddemo.online.dto.LoginResponse;
import com.carddemo.online.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AuthenticationException("Invalid user ID or password"));

        if (!request.getPassword().equals(user.getPwd())) {
            throw new AuthenticationException("Invalid user ID or password");
        }

        String token = jwtUtil.generateToken(user.getUsrId(), user.getUsrType());
        return new LoginResponse(token, user.getUsrId(), user.getUsrType());
    }

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
