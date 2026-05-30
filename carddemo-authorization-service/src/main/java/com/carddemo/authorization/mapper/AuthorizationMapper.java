package com.carddemo.authorization.mapper;

import com.carddemo.authorization.dto.AuthorizationDto;
import com.carddemo.authorization.entity.Authorization;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthorizationMapper {

    AuthorizationDto toDto(Authorization entity);

    Authorization toEntity(AuthorizationDto dto);

    List<AuthorizationDto> toDtoList(List<Authorization> entities);
}
