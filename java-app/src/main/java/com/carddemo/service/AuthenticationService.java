package com.carddemo.service;

import com.carddemo.model.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserSecurityRepository userSecurityRepository;

    public AuthenticationService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    public Optional<UserSecurity> authenticate(String userId, String password) {
        Optional<UserSecurity> user = userSecurityRepository.findById(userId);
        if (user.isPresent() && user.get().getPassword().trim().equals(password.trim())) {
            return user;
        }
        return Optional.empty();
    }

    public Optional<UserSecurity> findUser(String userId) {
        return userSecurityRepository.findById(userId);
    }

    public UserSecurity saveUser(UserSecurity user) {
        return userSecurityRepository.save(user);
    }

    public void deleteUser(String userId) {
        userSecurityRepository.deleteById(userId);
    }

    public java.util.List<UserSecurity> findAllUsers() {
        return userSecurityRepository.findAll();
    }
}
