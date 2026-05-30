package com.carddemo.account.mapper;

import com.carddemo.account.entity.Account;
import com.carddemo.common.dto.AccountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountMapperTest {

    private AccountMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(AccountMapper.class);
    }

    @Test
    void testToDto() {
        Account entity = Account.builder()
                .acctId(12345678901L)
                .acctActiveStatus("Y")
                .acctCurrBal(new BigDecimal("1000.50"))
                .acctCreditLimit(new BigDecimal("5000.00"))
                .acctCashCreditLimit(new BigDecimal("2000.00"))
                .acctOpenDate("2020-01-15")
                .acctExpirationDate("2025-01-15")
                .acctReissueDate("2023-01-15")
                .acctCurrCycCredit(new BigDecimal("200.00"))
                .acctCurrCycDebit(new BigDecimal("150.00"))
                .acctAddrZip("10001")
                .acctGroupId("GRP001")
                .build();

        AccountDto dto = mapper.toDto(entity);

        assertEquals(entity.getAcctId(), dto.getAcctId());
        assertEquals(entity.getAcctActiveStatus(), dto.getAcctActiveStatus());
        assertEquals(entity.getAcctCurrBal(), dto.getAcctCurrBal());
        assertEquals(entity.getAcctCreditLimit(), dto.getAcctCreditLimit());
        assertEquals(entity.getAcctCashCreditLimit(), dto.getAcctCashCreditLimit());
        assertEquals(entity.getAcctOpenDate(), dto.getAcctOpenDate());
        assertEquals(entity.getAcctExpirationDate(), dto.getAcctExpirationDate());
        assertEquals(entity.getAcctReissueDate(), dto.getAcctReissueDate());
        assertEquals(entity.getAcctCurrCycCredit(), dto.getAcctCurrCycCredit());
        assertEquals(entity.getAcctCurrCycDebit(), dto.getAcctCurrCycDebit());
        assertEquals(entity.getAcctAddrZip(), dto.getAcctAddrZip());
        assertEquals(entity.getAcctGroupId(), dto.getAcctGroupId());
    }

    @Test
    void testToEntity() {
        AccountDto dto = AccountDto.builder()
                .acctId(1L)
                .acctActiveStatus("N")
                .acctCurrBal(new BigDecimal("500.00"))
                .build();

        Account entity = mapper.toEntity(dto);

        assertEquals(dto.getAcctId(), entity.getAcctId());
        assertEquals(dto.getAcctActiveStatus(), entity.getAcctActiveStatus());
        assertEquals(dto.getAcctCurrBal(), entity.getAcctCurrBal());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void testNullMapping() {
        assertNull(mapper.toDto(null));
        assertNull(mapper.toEntity(null));
    }
}
