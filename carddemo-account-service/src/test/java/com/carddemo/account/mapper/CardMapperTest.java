package com.carddemo.account.mapper;

import com.carddemo.account.entity.Card;
import com.carddemo.common.dto.CardDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CardMapperTest {

    private CardMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CardMapper.class);
    }

    @Test
    void testToDto() {
        Card entity = Card.builder()
                .cardNum("4111111111111111")
                .cardAcctId(12345678901L)
                .cardCvvCd(123)
                .cardEmbossedName("JOHN DOE")
                .cardExpirationDate("2025-12-31")
                .cardActiveStatus("Y")
                .build();

        CardDto dto = mapper.toDto(entity);

        assertEquals(entity.getCardNum(), dto.getCardNum());
        assertEquals(entity.getCardAcctId(), dto.getCardAcctId());
        assertEquals(entity.getCardCvvCd(), dto.getCardCvvCd());
        assertEquals(entity.getCardEmbossedName(), dto.getCardEmbossedName());
        assertEquals(entity.getCardExpirationDate(), dto.getCardExpirationDate());
        assertEquals(entity.getCardActiveStatus(), dto.getCardActiveStatus());
    }

    @Test
    void testToEntity() {
        CardDto dto = CardDto.builder()
                .cardNum("4111111111111111")
                .cardAcctId(1L)
                .cardCvvCd(456)
                .cardEmbossedName("JANE DOE")
                .cardExpirationDate("2026-06-30")
                .cardActiveStatus("N")
                .build();

        Card entity = mapper.toEntity(dto);

        assertEquals(dto.getCardNum(), entity.getCardNum());
        assertEquals(dto.getCardAcctId(), entity.getCardAcctId());
        assertEquals(dto.getCardCvvCd(), entity.getCardCvvCd());
        assertEquals(dto.getCardEmbossedName(), entity.getCardEmbossedName());
        assertEquals(dto.getCardExpirationDate(), entity.getCardExpirationDate());
        assertEquals(dto.getCardActiveStatus(), entity.getCardActiveStatus());
        assertNull(entity.getAccount());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void testNullMapping() {
        assertNull(mapper.toDto(null));
        assertNull(mapper.toEntity(null));
    }
}
