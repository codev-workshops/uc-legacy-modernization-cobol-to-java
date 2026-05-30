package com.carddemo.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void builderAndGetters() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .message("Not Found")
                .details("Resource with id 1 not found")
                .timestamp(now)
                .build();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("Not Found");
        assertThat(response.getDetails()).isEqualTo("Resource with id 1 not found");
        assertThat(response.getTimestamp()).isEqualTo(now);
    }

    @Test
    void serializationDeserialization() throws Exception {
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        ErrorResponse response = ErrorResponse.builder()
                .status(500)
                .message("Internal Server Error")
                .details("Something went wrong")
                .timestamp(now)
                .build();

        String json = objectMapper.writeValueAsString(response);
        ErrorResponse deserialized = objectMapper.readValue(json, ErrorResponse.class);

        assertThat(deserialized.getStatus()).isEqualTo(500);
        assertThat(deserialized.getMessage()).isEqualTo("Internal Server Error");
        assertThat(deserialized.getDetails()).isEqualTo("Something went wrong");
        assertThat(deserialized.getTimestamp()).isEqualTo(now);
    }

    @Test
    void equalsAndHashCode() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        ErrorResponse r1 = ErrorResponse.builder().status(404).message("NF").timestamp(now).build();
        ErrorResponse r2 = ErrorResponse.builder().status(404).message("NF").timestamp(now).build();
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        ErrorResponse response = ErrorResponse.builder().status(400).message("Bad Request").build();
        assertThat(response.toString()).contains("status=400", "message=Bad Request");
    }

    @Test
    void setters() {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(403);
        response.setMessage("Forbidden");
        response.setDetails("Access denied");
        response.setTimestamp(LocalDateTime.of(2024, 6, 1, 12, 0));
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getMessage()).isEqualTo("Forbidden");
    }
}
