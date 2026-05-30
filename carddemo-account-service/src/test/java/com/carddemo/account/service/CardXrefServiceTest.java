package com.carddemo.account.service;

import com.carddemo.account.entity.CardXref;
import com.carddemo.account.mapper.CardXrefMapper;
import com.carddemo.account.repository.CardXrefRepository;
import com.carddemo.common.dto.CardXrefDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardXrefServiceTest {

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private CardXrefMapper cardXrefMapper;

    @InjectMocks
    private CardXrefService cardXrefService;

    @Test
    void getCardXref_found() {
        CardXref xref = CardXref.builder()
                .xrefCardNum("4111111111111111")
                .xrefCustId(100L)
                .xrefAcctId(1L)
                .build();
        CardXrefDto dto = CardXrefDto.builder()
                .xrefCardNum("4111111111111111")
                .xrefCustId(100L)
                .xrefAcctId(1L)
                .build();

        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(cardXrefMapper.toDto(xref)).thenReturn(dto);

        CardXrefDto result = cardXrefService.getCardXref("4111111111111111");

        assertThat(result.getXrefCardNum()).isEqualTo("4111111111111111");
        assertThat(result.getXrefAcctId()).isEqualTo(1L);
    }

    @Test
    void getCardXref_notFound() {
        when(cardXrefRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardXrefService.getCardXref("0000000000000000"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Card xref not found");
    }
}
