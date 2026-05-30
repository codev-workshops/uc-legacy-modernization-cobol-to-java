package com.carddemo.online.service;

import com.carddemo.common.entity.User;
import com.carddemo.common.repository.UserRepository;
import com.carddemo.online.dto.UserRequest;
import com.carddemo.online.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void listUsers_returnsAll() {
        User user = new User();
        user.setUsrId("USR01");
        user.setFname("John");
        user.setLname("Doe");
        user.setUsrType("U");
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userService.listUsers();

        assertEquals(1, result.size());
        assertEquals("USR01", result.get(0).getUsrId());
    }

    @Test
    void addUser_success() {
        when(userRepository.existsById("NEW01")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserRequest request = createRequest("NEW01", "Jane", "Doe", "pwd", "U");
        UserResponse response = userService.addUser(request);

        assertNotNull(response);
        assertEquals("NEW01", response.getUsrId());
        assertEquals("Jane", response.getFname());
    }

    @Test
    void addUser_duplicate_throws() {
        when(userRepository.existsById("DUP01")).thenReturn(true);

        UserRequest request = createRequest("DUP01", "J", "D", "p", "U");

        assertThrows(UserService.UserAlreadyExistsException.class,
                () -> userService.addUser(request));
    }

    @Test
    void updateUser_success() {
        User existing = new User();
        existing.setUsrId("USR01");
        existing.setFname("Old");
        existing.setLname("Name");
        existing.setPwd("oldpwd");
        existing.setUsrType("U");

        when(userRepository.findById("USR01")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserRequest request = createRequest("USR01", "New", "Name", "newpwd", "A");
        UserResponse response = userService.updateUser("USR01", request);

        assertEquals("New", response.getFname());
        assertEquals("A", response.getUsrType());
    }

    @Test
    void updateUser_notFound_throws() {
        when(userRepository.findById("NOPE")).thenReturn(Optional.empty());

        UserRequest request = createRequest("NOPE", "X", "Y", "p", "U");

        assertThrows(UserService.UserNotFoundException.class,
                () -> userService.updateUser("NOPE", request));
    }

    @Test
    void deleteUser_success() {
        when(userRepository.existsById("USR01")).thenReturn(true);

        userService.deleteUser("USR01");

        verify(userRepository).deleteById("USR01");
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepository.existsById("NOPE")).thenReturn(false);

        assertThrows(UserService.UserNotFoundException.class,
                () -> userService.deleteUser("NOPE"));
    }

    private UserRequest createRequest(String id, String fname, String lname,
                                      String pwd, String type) {
        UserRequest r = new UserRequest();
        r.setUsrId(id);
        r.setFname(fname);
        r.setLname(lname);
        r.setPwd(pwd);
        r.setUsrType(type);
        return r;
    }
}
