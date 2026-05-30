package com.carddemo.account.mapper;

import com.carddemo.account.entity.Card;
import com.carddemo.common.dto.CardDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Card toEntity(CardDto dto);

    CardDto toDto(Card entity);
}
