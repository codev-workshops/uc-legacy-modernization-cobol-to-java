package com.mainframe.carddemo.auth.service;

import com.mainframe.carddemo.auth.dto.LoginRequest;
import com.mainframe.carddemo.auth.dto.LoginResponse;
import com.mainframe.carddemo.auth.entity.UserSecurity;
import com.mainframe.carddemo.auth.repository.UserSecurityRepository;
import com.mainframe.carddemo.auth.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserSecurityRepository userSecurityRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        UserSecurity user = userSecurityRepository.findById(request.getUserId())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getUsrPwd())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String userType = "A".equals(user.getUsrType()) ? "ADMIN" : "USER";
        String token = jwtTokenProvider.generateToken(user.getUsrId(), user.getUsrType());

        return new LoginResponse(token, user.getUsrId(), userType, jwtTokenProvider.getExpirationMs());
    }
}
