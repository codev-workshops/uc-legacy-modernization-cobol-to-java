package com.mainframe.carddemo.auth.service;

import com.mainframe.carddemo.auth.dto.UpdateUserRequest;
import com.mainframe.carddemo.auth.dto.UserRequest;
import com.mainframe.carddemo.auth.dto.UserResponse;
import com.mainframe.carddemo.auth.entity.UserSecurity;
import com.mainframe.carddemo.auth.repository.UserSecurityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserSecurityRepository userSecurityRepository,
                       PasswordEncoder passwordEncoder) {
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserResponse> listUsers(Pageable pageable) {
        return userSecurityRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public UserResponse createUser(UserRequest request) {
        if (userSecurityRepository.existsById(request.getUserId())) {
            throw new UserAlreadyExistsException("User already exists: " + request.getUserId());
        }

        UserSecurity user = new UserSecurity();
        user.setUsrId(request.getUserId());
        user.setUsrFname(request.getFirstName());
        user.setUsrLname(request.getLastName());
        user.setUsrPwd(passwordEncoder.encode(request.getPassword()));
        user.setUsrType(request.getUserType());

        userSecurityRepository.save(user);
        return toResponse(user);
    }

    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        UserSecurity user = userSecurityRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (request.getFirstName() != null) {
            user.setUsrFname(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setUsrLname(request.getLastName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setUsrPwd(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getUserType() != null) {
            user.setUsrType(request.getUserType());
        }

        userSecurityRepository.save(user);
        return toResponse(user);
    }

    public void deleteUser(String userId) {
        if (!userSecurityRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        userSecurityRepository.deleteById(userId);
    }

    private UserResponse toResponse(UserSecurity user) {
        String type = "A".equals(user.getUsrType()) ? "ADMIN" : "USER";
        return new UserResponse(user.getUsrId(), user.getUsrFname(), user.getUsrLname(), type);
    }
}
