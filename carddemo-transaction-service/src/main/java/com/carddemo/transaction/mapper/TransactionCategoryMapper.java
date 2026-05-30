package com.carddemo.transaction.mapper;

import com.carddemo.common.dto.TransactionCategoryDto;
import com.carddemo.transaction.entity.TransactionCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionCategoryMapper {

    @Mapping(target = "createdAt", ignore = true)
    TransactionCategory toEntity(TransactionCategoryDto dto);

    TransactionCategoryDto toDto(TransactionCategory entity);
}
