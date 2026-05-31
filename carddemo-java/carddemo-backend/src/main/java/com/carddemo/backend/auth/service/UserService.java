package com.carddemo.backend.auth.service;

import com.carddemo.backend.auth.dto.CreateUserRequest;
import com.carddemo.backend.auth.dto.UpdateUserRequest;
import com.carddemo.backend.auth.entity.UserEntity;
import com.carddemo.backend.auth.repository.UserRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserEntity> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public UserEntity findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
    }

    @Transactional
    public UserEntity createUser(CreateUserRequest request) {
        if (userRepository.existsById(request.getUserId())) {
            throw new ValidationException("User with ID '" + request.getUserId() + "' already exists");
        }

        UserEntity user = new UserEntity();
        user.setUserId(request.getUserId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserType(request.getUserType());
        return userRepository.save(user);
    }

    @Transactional
    public UserEntity updateUser(String userId, UpdateUserRequest request) {
        UserEntity user = findById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "userId", userId);
        }
        userRepository.deleteById(userId);
    }
}
