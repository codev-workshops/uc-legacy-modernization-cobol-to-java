package com.carddemo.transaction.service;

import com.carddemo.common.dto.TransactionTypeDto;
import com.carddemo.common.exception.DuplicateResourceException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.transaction.entity.TransactionType;
import com.carddemo.transaction.mapper.TransactionTypeMapper;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionTypeServiceTest {

    @Mock
    private TransactionTypeRepository transactionTypeRepository;

    @Mock
    private TransactionTypeMapper transactionTypeMapper;

    @InjectMocks
    private TransactionTypeService transactionTypeService;

    @Test
    void listTransactionTypes() {
        TransactionType type = TransactionType.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        when(transactionTypeRepository.findAll()).thenReturn(List.of(type));
        when(transactionTypeMapper.toDto(type)).thenReturn(dto);

        List<TransactionTypeDto> result = transactionTypeService.listTransactionTypes();

        assertEquals(1, result.size());
        assertEquals("01", result.get(0).getTranType());
    }

    @Test
    void getTransactionType_found() {
        TransactionType type = TransactionType.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        when(transactionTypeRepository.findById("01")).thenReturn(Optional.of(type));
        when(transactionTypeMapper.toDto(type)).thenReturn(dto);

        TransactionTypeDto result = transactionTypeService.getTransactionType("01");
        assertEquals("01", result.getTranType());
    }

    @Test
    void getTransactionType_notFound() {
        when(transactionTypeRepository.findById("99")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionTypeService.getTransactionType("99"));
    }

    @Test
    void createTransactionType_success() {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();
        TransactionType entity = TransactionType.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        when(transactionTypeRepository.existsById("01")).thenReturn(false);
        when(transactionTypeMapper.toEntity(dto)).thenReturn(entity);
        when(transactionTypeRepository.save(any())).thenReturn(entity);
        when(transactionTypeMapper.toDto(entity)).thenReturn(dto);

        TransactionTypeDto result = transactionTypeService.createTransactionType(dto);
        assertEquals("01", result.getTranType());
    }

    @Test
    void createTransactionType_duplicate() {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .build();

        when(transactionTypeRepository.existsById("01")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> transactionTypeService.createTransactionType(dto));
    }

    @Test
    void updateTransactionType_success() {
        TransactionType existing = TransactionType.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .createdAt(LocalDateTime.now())
                .build();
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .tranTypeDesc("Updated Purchase")
                .build();
        TransactionType updated = TransactionType.builder()
                .tranType("01")
                .tranTypeDesc("Updated Purchase")
                .build();

        when(transactionTypeRepository.findById("01")).thenReturn(Optional.of(existing));
        when(transactionTypeRepository.save(any())).thenReturn(updated);
        when(transactionTypeMapper.toDto(updated)).thenReturn(dto);

        TransactionTypeDto result = transactionTypeService.updateTransactionType("01", dto);
        assertEquals("Updated Purchase", result.getTranTypeDesc());
    }

    @Test
    void updateTransactionType_notFound() {
        when(transactionTypeRepository.findById("99")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionTypeService.updateTransactionType("99",
                        TransactionTypeDto.builder().build()));
    }

    @Test
    void deleteTransactionType_success() {
        when(transactionTypeRepository.existsById("01")).thenReturn(true);

        transactionTypeService.deleteTransactionType("01");

        verify(transactionTypeRepository).deleteById("01");
    }

    @Test
    void deleteTransactionType_notFound() {
        when(transactionTypeRepository.existsById("99")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> transactionTypeService.deleteTransactionType("99"));
    }
}
