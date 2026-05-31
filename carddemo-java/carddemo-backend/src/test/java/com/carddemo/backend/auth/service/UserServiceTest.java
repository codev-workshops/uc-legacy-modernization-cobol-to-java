package com.carddemo.backend.auth.service;

import com.carddemo.backend.auth.dto.CreateUserRequest;
import com.carddemo.backend.auth.dto.UpdateUserRequest;
import com.carddemo.backend.auth.entity.UserEntity;
import com.carddemo.backend.auth.repository.UserRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setUserId("USER0001");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setUserType("U");
    }

    @Test
    void findAll_returnsPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<UserEntity> result = userService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("USER0001", result.getContent().get(0).getUserId());
    }

    @Test
    void findById_existingUser_returnsUser() {
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(testUser));

        UserEntity result = userService.findById("USER0001");

        assertNotNull(result);
        assertEquals("USER0001", result.getUserId());
    }

    @Test
    void findById_nonExistingUser_throwsResourceNotFoundException() {
        when(userRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById("INVALID"));
    }

    @Test
    void createUser_validRequest_createsUser() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserId("NEWUSER1");
        request.setFirstName("New");
        request.setLastName("User");
        request.setPassword("password123");
        request.setUserType("U");

        when(userRepository.existsById("NEWUSER1")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("NEWUSER1", result.getUserId());
        assertEquals("$2a$10$encoded", result.getPasswordHash());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void createUser_duplicateId_throwsValidationException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserId("USER0001");
        request.setFirstName("Dup");
        request.setLastName("User");
        request.setPassword("password123");
        request.setUserType("U");

        when(userRepository.existsById("USER0001")).thenReturn(true);

        assertThrows(ValidationException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_existingUser_updatesFields() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");

        when(userRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.updateUser("USER0001", request);

        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
    }

    @Test
    void updateUser_withPassword_encodesPassword() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("newpassword");

        when(userRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("$2a$10$newencoded");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.updateUser("USER0001", request);

        assertEquals("$2a$10$newencoded", result.getPasswordHash());
    }

    @Test
    void updateUser_nonExistingUser_throwsResourceNotFoundException() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");

        when(userRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser("INVALID", request));
    }

    @Test
    void deleteUser_existingUser_deletesUser() {
        when(userRepository.existsById("USER0001")).thenReturn(true);

        userService.deleteUser("USER0001");

        verify(userRepository).deleteById("USER0001");
    }

    @Test
    void deleteUser_nonExistingUser_throwsResourceNotFoundException() {
        when(userRepository.existsById("INVALID")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser("INVALID"));
        verify(userRepository, never()).deleteById(anyString());
    }

    @Test
    void updateUser_withUserType_updatesUserType() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUserType("A");

        when(userRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.updateUser("USER0001", request);

        assertEquals("A", result.getUserType());
    }

    @Test
    void updateUser_nullFields_doesNotUpdate() {
        UpdateUserRequest request = new UpdateUserRequest();

        when(userRepository.findById("USER0001")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.updateUser("USER0001", request);

        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("U", result.getUserType());
    }
}
