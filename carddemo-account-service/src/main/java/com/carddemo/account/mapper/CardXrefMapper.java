package com.carddemo.account.mapper;

import com.carddemo.account.entity.CardXref;
import com.carddemo.common.dto.CardXrefDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardXrefMapper {

    @Mapping(target = "createdAt", ignore = true)
    CardXref toEntity(CardXrefDto dto);

    CardXrefDto toDto(CardXref entity);
}
