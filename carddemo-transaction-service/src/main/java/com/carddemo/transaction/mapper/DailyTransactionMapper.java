package com.carddemo.transaction.mapper;

import com.carddemo.common.dto.DailyTransactionDto;
import com.carddemo.transaction.entity.DailyTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DailyTransactionMapper {

    @Mapping(target = "createdAt", ignore = true)
    DailyTransaction toEntity(DailyTransactionDto dto);

    DailyTransactionDto toDto(DailyTransaction entity);
}
