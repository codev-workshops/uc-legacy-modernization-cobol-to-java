package com.carddemo.controller;

import com.carddemo.model.UserSecurity;
import com.carddemo.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String userId = credentials.get("userId");
        String password = credentials.get("password");

        return authService.authenticate(userId, password)
                .map(user -> ResponseEntity.ok(Map.<String, Object>of(
                        "authenticated", true,
                        "userId", user.getUserId().trim(),
                        "firstName", user.getFirstName().trim(),
                        "lastName", user.getLastName().trim(),
                        "userType", user.getUserType().trim()
                )))
                .orElse(ResponseEntity.status(401).body(Map.of(
                        "authenticated", false,
                        "message", "Invalid credentials"
                )));
    }

    @GetMapping("/users")
    public List<UserSecurity> getAllUsers() {
        return authService.findAllUsers();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserSecurity> getUser(@PathVariable String userId) {
        return authService.findUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public UserSecurity createUser(@RequestBody UserSecurity user) {
        return authService.saveUser(user);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        return authService.findUser(userId)
                .map(existing -> {
                    authService.deleteUser(userId);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
