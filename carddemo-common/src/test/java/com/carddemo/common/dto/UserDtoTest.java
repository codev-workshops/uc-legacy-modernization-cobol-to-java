package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoTest {

    @Test
    void builderAndGetters() {
        UserDto dto = UserDto.builder()
                .userId("USER0001")
                .firstName("John")
                .lastName("Doe")
                .password("hashed01")
                .userType("A")
                .build();

        assertThat(dto.getUserId()).isEqualTo("USER0001");
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getPassword()).isEqualTo("hashed01");
        assertThat(dto.getUserType()).isEqualTo("A");
    }

    @Test
    void equalsAndHashCode() {
        UserDto dto1 = UserDto.builder().userId("U1").firstName("John").build();
        UserDto dto2 = UserDto.builder().userId("U1").firstName("John").build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        UserDto dto = UserDto.builder().userId("U1").build();
        assertThat(dto.toString()).contains("userId=U1");
    }

    @Test
    void setters() {
        UserDto dto = new UserDto();
        dto.setUserId("U2");
        dto.setFirstName("Jane");
        assertThat(dto.getUserId()).isEqualTo("U2");
        assertThat(dto.getFirstName()).isEqualTo("Jane");
    }
}
