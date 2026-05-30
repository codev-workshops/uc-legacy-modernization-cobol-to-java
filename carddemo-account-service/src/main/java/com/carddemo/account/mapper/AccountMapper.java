package com.carddemo.account.mapper;

import com.carddemo.account.entity.Account;
import com.carddemo.common.dto.AccountDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account toEntity(AccountDto dto);

    AccountDto toDto(Account entity);
}
