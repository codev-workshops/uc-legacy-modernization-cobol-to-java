package com.carddemo.transaction.mapper;

import com.carddemo.common.dto.TransactionTypeDto;
import com.carddemo.transaction.entity.TransactionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionTypeMapper {

    @Mapping(target = "createdAt", ignore = true)
    TransactionType toEntity(TransactionTypeDto dto);

    TransactionTypeDto toDto(TransactionType entity);
}
