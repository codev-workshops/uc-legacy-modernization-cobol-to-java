package com.carddemo.auth.service;

import com.carddemo.auth.entity.User;
import com.carddemo.auth.mapper.UserMapper;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.common.dto.UserDto;
import com.carddemo.common.exception.DuplicateResourceException;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void listUsers_shouldReturnPaginatedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = User.builder().userId("user01").firstName("Test").build();
        Page<User> userPage = new PageImpl<>(List.of(user));
        UserDto dto = UserDto.builder().userId("user01").firstName("Test").build();

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(dto);

        Page<UserDto> result = userService.listUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo("user01");
    }

    @Test
    void getUser_withExistingUser_shouldReturnDto() {
        User user = User.builder().userId("user01").firstName("Test").build();
        UserDto dto = UserDto.builder().userId("user01").firstName("Test").build();

        when(userRepository.findByUserId("user01")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userService.getUser("user01");

        assertThat(result.getUserId()).isEqualTo("user01");
    }

    @Test
    void getUser_withNonExistingUser_shouldThrowException() {
        when(userRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createUser_shouldHashPasswordAndSave() {
        UserDto dto = UserDto.builder()
                .userId("new01")
                .firstName("New")
                .lastName("User")
                .password("plaintext")
                .userType("U")
                .build();
        User entity = User.builder()
                .userId("new01")
                .firstName("New")
                .lastName("User")
                .userType("U")
                .build();
        User saved = User.builder()
                .userId("new01")
                .firstName("New")
                .lastName("User")
                .userType("U")
                .password("hashed")
                .build();
        UserDto resultDto = UserDto.builder()
                .userId("new01")
                .firstName("New")
                .lastName("User")
                .userType("U")
                .build();

        when(userRepository.findByUserId("new01")).thenReturn(Optional.empty());
        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(passwordEncoder.encode("plaintext")).thenReturn("hashed");
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toDto(saved)).thenReturn(resultDto);

        UserDto result = userService.createUser(dto);

        assertThat(result.getUserId()).isEqualTo("new01");
        verify(passwordEncoder).encode("plaintext");
    }

    @Test
    void createUser_withDuplicate_shouldThrowException() {
        UserDto dto = UserDto.builder().userId("existing").build();
        when(userRepository.findByUserId("existing")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User already exists");
    }

    @Test
    void updateUser_shouldUpdateFieldsAndSave() {
        User existing = User.builder()
                .userId("user01")
                .firstName("Old")
                .lastName("Name")
                .userType("U")
                .password("oldHash")
                .build();
        UserDto updateDto = UserDto.builder()
                .firstName("New")
                .lastName("Name")
                .password("newpass")
                .userType("A")
                .build();
        UserDto resultDto = UserDto.builder()
                .userId("user01")
                .firstName("New")
                .lastName("Name")
                .userType("A")
                .build();

        when(userRepository.findByUserId("user01")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn("newHash");
        when(userRepository.save(any(User.class))).thenReturn(existing);
        when(userMapper.toDto(any(User.class))).thenReturn(resultDto);

        UserDto result = userService.updateUser("user01", updateDto);

        assertThat(result.getFirstName()).isEqualTo("New");
        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void updateUser_withNonExisting_shouldThrowException() {
        when(userRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser("unknown", new UserDto()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUser_shouldDeleteExistingUser() {
        User user = User.builder().userId("user01").build();
        when(userRepository.findByUserId("user01")).thenReturn(Optional.of(user));

        userService.deleteUser("user01");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_withNonExisting_shouldThrowException() {
        when(userRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
