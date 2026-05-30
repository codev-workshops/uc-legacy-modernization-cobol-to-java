package com.carddemo.common.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void builderAndGetters() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("OK")
                .data("result")
                .build();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getData()).isEqualTo("result");
    }

    @Test
    void serializationDeserialization() throws Exception {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Created")
                .data("new item")
                .build();

        String json = objectMapper.writeValueAsString(response);
        ApiResponse<String> deserialized = objectMapper.readValue(json,
                new TypeReference<ApiResponse<String>>() {});

        assertThat(deserialized.isSuccess()).isTrue();
        assertThat(deserialized.getMessage()).isEqualTo("Created");
        assertThat(deserialized.getData()).isEqualTo("new item");
    }

    @Test
    void equalsAndHashCode() {
        ApiResponse<String> r1 = ApiResponse.<String>builder().success(true).message("OK").data("d").build();
        ApiResponse<String> r2 = ApiResponse.<String>builder().success(true).message("OK").data("d").build();
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        ApiResponse<String> response = ApiResponse.<String>builder().success(true).message("OK").build();
        assertThat(response.toString()).contains("success=true", "message=OK");
    }

    @Test
    void setters() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("Error");
        response.setData("err data");
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Error");
        assertThat(response.getData()).isEqualTo("err data");
    }
}
