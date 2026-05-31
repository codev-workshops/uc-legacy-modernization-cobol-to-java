package com.carddemo.backend.account;

import com.carddemo.backend.account.entity.CardXrefEntity;
import com.carddemo.backend.account.repository.CardXrefRepository;
import com.carddemo.backend.account.service.CardXrefService;
import com.carddemo.common.dto.CardXrefDto;
import com.carddemo.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardXrefServiceTest {

    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private CardXrefService cardXrefService;

    @Test
    void findByCardNum_returnsDto() {
        CardXrefEntity entity = new CardXrefEntity();
        entity.setXrefCardNum("0500024453765740");
        entity.setXrefCustId(50L);
        entity.setXrefAcctId(50L);

        when(cardXrefRepository.findByXrefCardNum("0500024453765740"))
                .thenReturn(Optional.of(entity));

        CardXrefDto result = cardXrefService.findByCardNum("0500024453765740");

        assertEquals("0500024453765740", result.getCardNumber());
        assertEquals(50L, result.getCustomerId());
        assertEquals(50L, result.getAccountId());
    }

    @Test
    void findByCardNum_notFound() {
        when(cardXrefRepository.findByXrefCardNum("9999999999999999"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cardXrefService.findByCardNum("9999999999999999"));
    }

    @Test
    void findByAcctId_returnsList() {
        CardXrefEntity e1 = new CardXrefEntity();
        e1.setXrefCardNum("0500024453765740");
        e1.setXrefCustId(50L);
        e1.setXrefAcctId(50L);

        CardXrefEntity e2 = new CardXrefEntity();
        e2.setXrefCardNum("0683586198171516");
        e2.setXrefCustId(27L);
        e2.setXrefAcctId(50L);

        when(cardXrefRepository.findByXrefAcctId(50L)).thenReturn(List.of(e1, e2));

        List<CardXrefDto> results = cardXrefService.findByAcctId(50L);

        assertEquals(2, results.size());
        assertEquals("0500024453765740", results.get(0).getCardNumber());
        assertEquals("0683586198171516", results.get(1).getCardNumber());
    }

    @Test
    void findByAcctId_emptyList() {
        when(cardXrefRepository.findByXrefAcctId(999L)).thenReturn(List.of());

        List<CardXrefDto> results = cardXrefService.findByAcctId(999L);

        assertTrue(results.isEmpty());
    }
}
