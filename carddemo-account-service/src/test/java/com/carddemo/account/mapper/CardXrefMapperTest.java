package com.carddemo.account.mapper;

import com.carddemo.account.entity.CardXref;
import com.carddemo.common.dto.CardXrefDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CardXrefMapperTest {

    private CardXrefMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CardXrefMapper.class);
    }

    @Test
    void testToDto() {
        CardXref entity = CardXref.builder()
                .xrefCardNum("4111111111111111")
                .xrefCustId(123456789L)
                .xrefAcctId(12345678901L)
                .build();

        CardXrefDto dto = mapper.toDto(entity);

        assertEquals(entity.getXrefCardNum(), dto.getXrefCardNum());
        assertEquals(entity.getXrefCustId(), dto.getXrefCustId());
        assertEquals(entity.getXrefAcctId(), dto.getXrefAcctId());
    }

    @Test
    void testToEntity() {
        CardXrefDto dto = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefCustId(1L)
                .xrefAcctId(2L)
                .build();

        CardXref entity = mapper.toEntity(dto);

        assertEquals(dto.getXrefCardNum(), entity.getXrefCardNum());
        assertEquals(dto.getXrefCustId(), entity.getXrefCustId());
        assertEquals(dto.getXrefAcctId(), entity.getXrefAcctId());
        assertNull(entity.getCreatedAt());
    }

    @Test
    void testNullMapping() {
        assertNull(mapper.toDto(null));
        assertNull(mapper.toEntity(null));
    }
}
