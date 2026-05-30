package com.carddemo.transaction.mapper;

import com.carddemo.common.dto.TransactionDto;
import com.carddemo.transaction.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "createdAt", ignore = true)
    Transaction toEntity(TransactionDto dto);

    TransactionDto toDto(Transaction entity);
}
