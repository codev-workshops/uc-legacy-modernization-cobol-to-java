package com.carddemo.online.service;

import com.carddemo.common.entity.User;
import com.carddemo.common.repository.UserRepository;
import com.carddemo.online.dto.UserRequest;
import com.carddemo.online.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse addUser(UserRequest request) {
        if (userRepository.existsById(request.getUsrId())) {
            throw new UserAlreadyExistsException(
                    "User already exists: " + request.getUsrId());
        }
        User user = toEntity(request);
        userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse updateUser(String id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        user.setFname(request.getFname());
        user.setLname(request.getLname());
        user.setPwd(request.getPwd());
        user.setUsrType(request.getUsrType());
        userRepository.save(user);
        return toResponse(user);
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUsrId(),
                user.getFname(),
                user.getLname(),
                user.getUsrType()
        );
    }

    private User toEntity(UserRequest request) {
        User user = new User();
        user.setUsrId(request.getUsrId());
        user.setFname(request.getFname());
        user.setLname(request.getLname());
        user.setPwd(request.getPwd());
        user.setUsrType(request.getUsrType());
        return user;
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }
}
