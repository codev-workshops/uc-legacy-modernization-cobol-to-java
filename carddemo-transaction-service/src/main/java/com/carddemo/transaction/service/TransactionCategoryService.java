package com.carddemo.transaction.service;

import com.carddemo.common.dto.TransactionCategoryDto;
import com.carddemo.common.exception.DuplicateResourceException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.transaction.entity.TransactionCategory;
import com.carddemo.transaction.entity.TransactionCategoryId;
import com.carddemo.transaction.mapper.TransactionCategoryMapper;
import com.carddemo.transaction.repository.TransactionCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionCategoryService {

    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionCategoryMapper transactionCategoryMapper;

    public List<TransactionCategoryDto> listTransactionCategories() {
        return transactionCategoryRepository.findAll().stream()
                .map(transactionCategoryMapper::toDto)
                .toList();
    }

    public TransactionCategoryDto getTransactionCategory(String typeCd, Integer catCd) {
        TransactionCategoryId id = new TransactionCategoryId(typeCd, catCd);
        TransactionCategory entity = transactionCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction category not found: " + typeCd + "/" + catCd));
        return transactionCategoryMapper.toDto(entity);
    }

    @Transactional
    public TransactionCategoryDto createTransactionCategory(TransactionCategoryDto dto) {
        TransactionCategoryId id = new TransactionCategoryId(dto.getTranTypeCd(), dto.getTranCatCd());
        if (transactionCategoryRepository.existsById(id)) {
            throw new DuplicateResourceException(
                    "Transaction category already exists: " + dto.getTranTypeCd() + "/" + dto.getTranCatCd());
        }
        TransactionCategory entity = transactionCategoryMapper.toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        TransactionCategory saved = transactionCategoryRepository.save(entity);
        log.info("Created transaction category: {}/{}", saved.getTranTypeCd(), saved.getTranCatCd());
        return transactionCategoryMapper.toDto(saved);
    }

    @Transactional
    public TransactionCategoryDto updateTransactionCategory(String typeCd, Integer catCd,
                                                            TransactionCategoryDto dto) {
        TransactionCategoryId id = new TransactionCategoryId(typeCd, catCd);
        TransactionCategory entity = transactionCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction category not found: " + typeCd + "/" + catCd));
        entity.setTranCatTypeDesc(dto.getTranCatTypeDesc());
        TransactionCategory saved = transactionCategoryRepository.save(entity);
        log.info("Updated transaction category: {}/{}", saved.getTranTypeCd(), saved.getTranCatCd());
        return transactionCategoryMapper.toDto(saved);
    }

    @Transactional
    public void deleteTransactionCategory(String typeCd, Integer catCd) {
        TransactionCategoryId id = new TransactionCategoryId(typeCd, catCd);
        if (!transactionCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Transaction category not found: " + typeCd + "/" + catCd);
        }
        transactionCategoryRepository.deleteById(id);
        log.info("Deleted transaction category: {}/{}", typeCd, catCd);
    }
}
