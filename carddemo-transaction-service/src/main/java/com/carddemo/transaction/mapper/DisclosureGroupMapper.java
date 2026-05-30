package com.carddemo.transaction.mapper;

import com.carddemo.common.dto.DisclosureGroupDto;
import com.carddemo.transaction.entity.DisclosureGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DisclosureGroupMapper {

    @Mapping(target = "createdAt", ignore = true)
    DisclosureGroup toEntity(DisclosureGroupDto dto);

    DisclosureGroupDto toDto(DisclosureGroup entity);
}
