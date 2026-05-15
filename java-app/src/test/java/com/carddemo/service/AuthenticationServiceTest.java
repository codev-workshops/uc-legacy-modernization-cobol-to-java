package com.carddemo.service;

import com.carddemo.model.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserSecurity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserSecurity();
        testUser.setUserId("ADMIN001");
        testUser.setFirstName("Admin");
        testUser.setLastName("User");
        testUser.setPassword("PASSWORD");
        testUser.setUserType("A");
    }

    @Test
    void authenticate_validCredentials_returnsUser() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(testUser));
        Optional<UserSecurity> result = authenticationService.authenticate("ADMIN001", "PASSWORD");
        assertTrue(result.isPresent());
        assertEquals("A", result.get().getUserType());
    }

    @Test
    void authenticate_invalidPassword_returnsEmpty() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(testUser));
        Optional<UserSecurity> result = authenticationService.authenticate("ADMIN001", "WRONG");
        assertFalse(result.isPresent());
    }

    @Test
    void authenticate_nonExistentUser_returnsEmpty() {
        when(userSecurityRepository.findById("NOBODY")).thenReturn(Optional.empty());
        Optional<UserSecurity> result = authenticationService.authenticate("NOBODY", "PASSWORD");
        assertFalse(result.isPresent());
    }
}
