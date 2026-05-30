package com.carddemo.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @NotNull
    @Size(max = 8)
    private String userId;

    @Size(max = 20)
    private String firstName;

    @Size(max = 20)
    private String lastName;

    @Size(max = 8)
    private String password;

    @Size(max = 1)
    private String userType;
}
