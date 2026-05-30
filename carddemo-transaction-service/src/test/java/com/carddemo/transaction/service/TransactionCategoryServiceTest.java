package com.carddemo.transaction.service;

import com.carddemo.common.dto.TransactionCategoryDto;
import com.carddemo.common.exception.DuplicateResourceException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.transaction.entity.TransactionCategory;
import com.carddemo.transaction.entity.TransactionCategoryId;
import com.carddemo.transaction.mapper.TransactionCategoryMapper;
import com.carddemo.transaction.repository.TransactionCategoryRepository;
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
class TransactionCategoryServiceTest {

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @Mock
    private TransactionCategoryMapper transactionCategoryMapper;

    @InjectMocks
    private TransactionCategoryService transactionCategoryService;

    @Test
    void listTransactionCategories() {
        TransactionCategory cat = TransactionCategory.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Retail")
                .build();
        TransactionCategoryDto dto = TransactionCategoryDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Retail")
                .build();

        when(transactionCategoryRepository.findAll()).thenReturn(List.of(cat));
        when(transactionCategoryMapper.toDto(cat)).thenReturn(dto);

        List<TransactionCategoryDto> result = transactionCategoryService.listTransactionCategories();

        assertEquals(1, result.size());
        assertEquals("Retail", result.get(0).getTranCatTypeDesc());
    }

    @Test
    void getTransactionCategory_found() {
        TransactionCategoryId id = new TransactionCategoryId("01", 1);
        TransactionCategory cat = TransactionCategory.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Retail")
                .build();
        TransactionCategoryDto dto = TransactionCategoryDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Retail")
                .build();

        when(transactionCategoryRepository.findById(id)).thenReturn(Optional.of(cat));
        when(transactionCategoryMapper.toDto(cat)).thenReturn(dto);

        TransactionCategoryDto result = transactionCategoryService.getTransactionCategory("01", 1);
        assertEquals("Retail", result.getTranCatTypeDesc());
    }

    @Test
    void getTransactionCategory_notFound() {
        TransactionCategoryId id = new TransactionCategoryId("99", 99);
        when(transactionCategoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionCategoryService.getTransactionCategory("99", 99));
    }

    @Test
    void createTransactionCategory_success() {
        TransactionCategoryDto dto = TransactionCategoryDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Retail")
                .build();
        TransactionCategory entity = TransactionCategory.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Retail")
                .build();

        TransactionCategoryId id = new TransactionCategoryId("01", 1);
        when(transactionCategoryRepository.existsById(id)).thenReturn(false);
        when(transactionCategoryMapper.toEntity(dto)).thenReturn(entity);
        when(transactionCategoryRepository.save(any())).thenReturn(entity);
        when(transactionCategoryMapper.toDto(entity)).thenReturn(dto);

        TransactionCategoryDto result = transactionCategoryService.createTransactionCategory(dto);
        assertEquals("Retail", result.getTranCatTypeDesc());
    }

    @Test
    void createTransactionCategory_duplicate() {
        TransactionCategoryDto dto = TransactionCategoryDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .build();

        TransactionCategoryId id = new TransactionCategoryId("01", 1);
        when(transactionCategoryRepository.existsById(id)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> transactionCategoryService.createTransactionCategory(dto));
    }

    @Test
    void updateTransactionCategory_success() {
        TransactionCategoryId id = new TransactionCategoryId("01", 1);
        TransactionCategory existing = TransactionCategory.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Retail")
                .createdAt(LocalDateTime.now())
                .build();
        TransactionCategoryDto dto = TransactionCategoryDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Updated Retail")
                .build();

        when(transactionCategoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(transactionCategoryRepository.save(any())).thenReturn(existing);
        when(transactionCategoryMapper.toDto(existing)).thenReturn(dto);

        TransactionCategoryDto result = transactionCategoryService
                .updateTransactionCategory("01", 1, dto);
        assertEquals("Updated Retail", result.getTranCatTypeDesc());
    }

    @Test
    void updateTransactionCategory_notFound() {
        TransactionCategoryId id = new TransactionCategoryId("99", 99);
        when(transactionCategoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionCategoryService.updateTransactionCategory("99", 99,
                        TransactionCategoryDto.builder().build()));
    }

    @Test
    void deleteTransactionCategory_success() {
        TransactionCategoryId id = new TransactionCategoryId("01", 1);
        when(transactionCategoryRepository.existsById(id)).thenReturn(true);

        transactionCategoryService.deleteTransactionCategory("01", 1);

        verify(transactionCategoryRepository).deleteById(id);
    }

    @Test
    void deleteTransactionCategory_notFound() {
        TransactionCategoryId id = new TransactionCategoryId("99", 99);
        when(transactionCategoryRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> transactionCategoryService.deleteTransactionCategory("99", 99));
    }
}
