package com.carddemo.transaction.mapper;

import com.carddemo.common.dto.TranCatBalanceDto;
import com.carddemo.transaction.entity.TranCatBalance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TranCatBalanceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TranCatBalance toEntity(TranCatBalanceDto dto);

    TranCatBalanceDto toDto(TranCatBalance entity);
}
