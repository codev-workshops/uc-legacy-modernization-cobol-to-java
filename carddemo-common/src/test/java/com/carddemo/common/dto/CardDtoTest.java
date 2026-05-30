package com.carddemo.common.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardDtoTest {

    @Test
    void builderAndGetters() {
        CardDto dto = CardDto.builder()
                .cardNum("4111111111111111")
                .cardAcctId(12345678901L)
                .cardCvvCd(123)
                .cardEmbossedName("JOHN DOE")
                .cardExpirationDate("2025-12-31")
                .cardActiveStatus("Y")
                .build();

        assertThat(dto.getCardNum()).isEqualTo("4111111111111111");
        assertThat(dto.getCardAcctId()).isEqualTo(12345678901L);
        assertThat(dto.getCardCvvCd()).isEqualTo(123);
        assertThat(dto.getCardEmbossedName()).isEqualTo("JOHN DOE");
        assertThat(dto.getCardExpirationDate()).isEqualTo("2025-12-31");
        assertThat(dto.getCardActiveStatus()).isEqualTo("Y");
    }

    @Test
    void equalsAndHashCode() {
        CardDto dto1 = CardDto.builder().cardNum("4111111111111111").cardAcctId(1L).build();
        CardDto dto2 = CardDto.builder().cardNum("4111111111111111").cardAcctId(1L).build();
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toStringContainsFields() {
        CardDto dto = CardDto.builder().cardNum("4111").build();
        assertThat(dto.toString()).contains("cardNum=4111");
    }

    @Test
    void setters() {
        CardDto dto = new CardDto();
        dto.setCardNum("1234567890123456");
        dto.setCardAcctId(99L);
        assertThat(dto.getCardNum()).isEqualTo("1234567890123456");
        assertThat(dto.getCardAcctId()).isEqualTo(99L);
    }
}
