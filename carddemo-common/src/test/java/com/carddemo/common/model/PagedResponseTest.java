package com.carddemo.common.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagedResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void builderAndGetters() {
        PagedResponse<String> response = PagedResponse.<String>builder()
                .content(List.of("a", "b", "c"))
                .page(0)
                .size(10)
                .totalElements(3)
                .totalPages(1)
                .build();

        assertThat(response.getContent()).containsExactly("a", "b", "c");
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(1);
    }

    @Test
    void serializationDeserialization() throws Exception {
        PagedResponse<String> response = PagedResponse.<String>builder()
                .content(List.of("item1", "item2"))
                .page(1)
                .size(5)
                .totalElements(12)
                .totalPages(3)
                .build();

        String json = objectMapper.writeValueAsString(response);
        PagedResponse<String> deserialized = objectMapper.readValue(json,
                new TypeReference<PagedResponse<String>>() {});

        assertThat(deserialized.getContent()).containsExactly("item1", "item2");
        assertThat(deserialized.getPage()).isEqualTo(1);
        assertThat(deserialized.getSize()).isEqualTo(5);
        assertThat(deserialized.getTotalElements()).isEqualTo(12);
        assertThat(deserialized.getTotalPages()).isEqualTo(3);
    }

    @Test
    void equalsAndHashCode() {
        PagedResponse<String> r1 = PagedResponse.<String>builder()
                .content(List.of("a")).page(0).size(10).totalElements(1).totalPages(1).build();
        PagedResponse<String> r2 = PagedResponse.<String>builder()
                .content(List.of("a")).page(0).size(10).totalElements(1).totalPages(1).build();
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        PagedResponse<String> response = PagedResponse.<String>builder().page(0).size(10).build();
        assertThat(response.toString()).contains("page=0", "size=10");
    }

    @Test
    void setters() {
        PagedResponse<String> response = new PagedResponse<>();
        response.setContent(List.of("x"));
        response.setPage(2);
        response.setSize(20);
        response.setTotalElements(100);
        response.setTotalPages(5);
        assertThat(response.getContent()).containsExactly("x");
        assertThat(response.getPage()).isEqualTo(2);
    }
}
