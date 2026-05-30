package com.carddemo.auth.mapper;

import com.carddemo.auth.entity.User;
import com.carddemo.common.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toDto_shouldMapEntityToDto() {
        User user = User.builder()
                .userId("user01")
                .firstName("John")
                .lastName("Doe")
                .password("hashed")
                .userType("U")
                .build();

        UserDto dto = userMapper.toDto(user);

        assertThat(dto.getUserId()).isEqualTo("user01");
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getPassword()).isNull();
        assertThat(dto.getUserType()).isEqualTo("U");
    }

    @Test
    void toEntity_shouldMapDtoToEntity() {
        UserDto dto = UserDto.builder()
                .userId("user01")
                .firstName("John")
                .lastName("Doe")
                .password("plain")
                .userType("U")
                .build();

        User entity = userMapper.toEntity(dto);

        assertThat(entity.getUserId()).isEqualTo("user01");
        assertThat(entity.getFirstName()).isEqualTo("John");
        assertThat(entity.getLastName()).isEqualTo("Doe");
        assertThat(entity.getPassword()).isEqualTo("plain");
        assertThat(entity.getUserType()).isEqualTo("U");
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void toDto_withNull_shouldReturnNull() {
        assertThat(userMapper.toDto(null)).isNull();
    }

    @Test
    void toEntity_withNull_shouldReturnNull() {
        assertThat(userMapper.toEntity(null)).isNull();
    }
}
