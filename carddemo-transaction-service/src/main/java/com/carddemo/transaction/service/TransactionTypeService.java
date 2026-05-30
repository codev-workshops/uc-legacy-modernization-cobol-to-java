package com.carddemo.transaction.service;

import com.carddemo.common.dto.TransactionTypeDto;
import com.carddemo.common.exception.DuplicateResourceException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.transaction.entity.TransactionType;
import com.carddemo.transaction.mapper.TransactionTypeMapper;
import com.carddemo.transaction.repository.TransactionTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionTypeService {

    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionTypeMapper transactionTypeMapper;

    public List<TransactionTypeDto> listTransactionTypes() {
        return transactionTypeRepository.findAll().stream()
                .map(transactionTypeMapper::toDto)
                .toList();
    }

    public TransactionTypeDto getTransactionType(String typeCode) {
        TransactionType entity = transactionTypeRepository.findById(typeCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction type not found: " + typeCode));
        return transactionTypeMapper.toDto(entity);
    }

    @Transactional
    public TransactionTypeDto createTransactionType(TransactionTypeDto dto) {
        if (transactionTypeRepository.existsById(dto.getTranType())) {
            throw new DuplicateResourceException(
                    "Transaction type already exists: " + dto.getTranType());
        }
        TransactionType entity = transactionTypeMapper.toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        TransactionType saved = transactionTypeRepository.save(entity);
        log.info("Created transaction type: {}", saved.getTranType());
        return transactionTypeMapper.toDto(saved);
    }

    @Transactional
    public TransactionTypeDto updateTransactionType(String typeCode, TransactionTypeDto dto) {
        TransactionType entity = transactionTypeRepository.findById(typeCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction type not found: " + typeCode));
        entity.setTranTypeDesc(dto.getTranTypeDesc());
        TransactionType saved = transactionTypeRepository.save(entity);
        log.info("Updated transaction type: {}", saved.getTranType());
        return transactionTypeMapper.toDto(saved);
    }

    @Transactional
    public void deleteTransactionType(String typeCode) {
        if (!transactionTypeRepository.existsById(typeCode)) {
            throw new ResourceNotFoundException(
                    "Transaction type not found: " + typeCode);
        }
        transactionTypeRepository.deleteById(typeCode);
        log.info("Deleted transaction type: {}", typeCode);
    }
}
