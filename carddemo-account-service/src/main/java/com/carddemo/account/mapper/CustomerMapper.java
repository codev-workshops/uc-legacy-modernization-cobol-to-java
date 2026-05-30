package com.carddemo.account.mapper;

import com.carddemo.account.entity.Customer;
import com.carddemo.common.dto.CustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerDto dto);

    CustomerDto toDto(Customer entity);
}
