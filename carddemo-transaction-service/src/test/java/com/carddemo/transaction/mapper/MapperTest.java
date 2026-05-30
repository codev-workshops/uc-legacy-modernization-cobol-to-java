package com.carddemo.transaction.mapper;

import com.carddemo.common.dto.*;
import com.carddemo.transaction.entity.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);
    private final DailyTransactionMapper dailyTransactionMapper = Mappers.getMapper(DailyTransactionMapper.class);
    private final TranCatBalanceMapper tranCatBalanceMapper = Mappers.getMapper(TranCatBalanceMapper.class);
    private final DisclosureGroupMapper disclosureGroupMapper = Mappers.getMapper(DisclosureGroupMapper.class);
    private final TransactionTypeMapper transactionTypeMapper = Mappers.getMapper(TransactionTypeMapper.class);
    private final TransactionCategoryMapper transactionCategoryMapper = Mappers.getMapper(TransactionCategoryMapper.class);

    @Test
    void testTransactionToDto() {
        Transaction entity = Transaction.builder()
                .tranId("TRN001")
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranSource("ONLINE")
                .tranDesc("Test")
                .tranAmt(new BigDecimal("100.00"))
                .tranMerchantId(12345L)
                .tranMerchantName("Merchant")
                .tranMerchantCity("City")
                .tranMerchantZip("12345")
                .tranCardNum("4111111111111111")
                .tranOrigTs("2024-01-15")
                .tranProcTs("2024-01-15")
                .createdAt(LocalDateTime.now())
                .build();

        TransactionDto dto = transactionMapper.toDto(entity);
        assertEquals("TRN001", dto.getTranId());
        assertEquals("01", dto.getTranTypeCd());
        assertEquals(1, dto.getTranCatCd());
        assertEquals(new BigDecimal("100.00"), dto.getTranAmt());
    }

    @Test
    void testTransactionToEntity() {
        TransactionDto dto = TransactionDto.builder()
                .tranId("TRN001")
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranAmt(new BigDecimal("100.00"))
                .tranCardNum("4111111111111111")
                .build();

        Transaction entity = transactionMapper.toEntity(dto);
        assertEquals("TRN001", entity.getTranId());
        assertNull(entity.getCreatedAt());
    }

    @Test
    void testDailyTransactionToDto() {
        DailyTransaction entity = DailyTransaction.builder()
                .dalytranId("DT001")
                .dalytranTypeCd("01")
                .dalytranCatCd(1)
                .dalytranAmt(new BigDecimal("50.00"))
                .dalytranCardNum("4111111111111111")
                .build();

        DailyTransactionDto dto = dailyTransactionMapper.toDto(entity);
        assertEquals("DT001", dto.getDalytranId());
        assertEquals(new BigDecimal("50.00"), dto.getDalytranAmt());
    }

    @Test
    void testDailyTransactionToEntity() {
        DailyTransactionDto dto = DailyTransactionDto.builder()
                .dalytranId("DT001")
                .dalytranTypeCd("01")
                .dalytranAmt(new BigDecimal("50.00"))
                .build();

        DailyTransaction entity = dailyTransactionMapper.toEntity(dto);
        assertEquals("DT001", entity.getDalytranId());
        assertNull(entity.getCreatedAt());
    }

    @Test
    void testTranCatBalanceToDto() {
        TranCatBalance entity = TranCatBalance.builder()
                .trancatAcctId(1000L)
                .trancatTypeCd("01")
                .trancatCd(1)
                .tranCatBal(new BigDecimal("5000.00"))
                .build();

        TranCatBalanceDto dto = tranCatBalanceMapper.toDto(entity);
        assertEquals(1000L, dto.getTrancatAcctId());
        assertEquals(new BigDecimal("5000.00"), dto.getTranCatBal());
    }

    @Test
    void testTranCatBalanceToEntity() {
        TranCatBalanceDto dto = TranCatBalanceDto.builder()
                .trancatAcctId(1000L)
                .trancatTypeCd("01")
                .trancatCd(1)
                .tranCatBal(new BigDecimal("5000.00"))
                .build();

        TranCatBalance entity = tranCatBalanceMapper.toEntity(dto);
        assertEquals(1000L, entity.getTrancatAcctId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void testDisclosureGroupToDto() {
        DisclosureGroup entity = DisclosureGroup.builder()
                .disAcctGroupId("GRP001")
                .disTranTypeCd("01")
                .disTranCatCd(1)
                .disIntRate(new BigDecimal("18.99"))
                .build();

        DisclosureGroupDto dto = disclosureGroupMapper.toDto(entity);
        assertEquals("GRP001", dto.getDisAcctGroupId());
        assertEquals(new BigDecimal("18.99"), dto.getDisIntRate());
    }

    @Test
    void testDisclosureGroupToEntity() {
        DisclosureGroupDto dto = DisclosureGroupDto.builder()
                .disAcctGroupId("GRP001")
                .disTranTypeCd("01")
                .disTranCatCd(1)
                .disIntRate(new BigDecimal("18.99"))
                .build();

        DisclosureGroup entity = disclosureGroupMapper.toEntity(dto);
        assertEquals("GRP001", entity.getDisAcctGroupId());
        assertNull(entity.getCreatedAt());
    }

    @Test
    void testTransactionTypeToDto() {
        TransactionType entity = TransactionType.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        TransactionTypeDto dto = transactionTypeMapper.toDto(entity);
        assertEquals("01", dto.getTranType());
        assertEquals("Purchase", dto.getTranTypeDesc());
    }

    @Test
    void testTransactionTypeToEntity() {
        TransactionTypeDto dto = TransactionTypeDto.builder()
                .tranType("01")
                .tranTypeDesc("Purchase")
                .build();

        TransactionType entity = transactionTypeMapper.toEntity(dto);
        assertEquals("01", entity.getTranType());
        assertNull(entity.getCreatedAt());
    }

    @Test
    void testTransactionCategoryToDto() {
        TransactionCategory entity = TransactionCategory.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Regular Sales Draft")
                .build();

        TransactionCategoryDto dto = transactionCategoryMapper.toDto(entity);
        assertEquals("01", dto.getTranTypeCd());
        assertEquals(1, dto.getTranCatCd());
        assertEquals("Regular Sales Draft", dto.getTranCatTypeDesc());
    }

    @Test
    void testTransactionCategoryToEntity() {
        TransactionCategoryDto dto = TransactionCategoryDto.builder()
                .tranTypeCd("01")
                .tranCatCd(1)
                .tranCatTypeDesc("Regular Sales Draft")
                .build();

        TransactionCategory entity = transactionCategoryMapper.toEntity(dto);
        assertEquals("01", entity.getTranTypeCd());
        assertNull(entity.getCreatedAt());
    }

    @Test
    void testNullMappings() {
        assertNull(transactionMapper.toDto(null));
        assertNull(transactionMapper.toEntity(null));
        assertNull(dailyTransactionMapper.toDto(null));
        assertNull(dailyTransactionMapper.toEntity(null));
        assertNull(tranCatBalanceMapper.toDto(null));
        assertNull(tranCatBalanceMapper.toEntity(null));
        assertNull(disclosureGroupMapper.toDto(null));
        assertNull(disclosureGroupMapper.toEntity(null));
        assertNull(transactionTypeMapper.toDto(null));
        assertNull(transactionTypeMapper.toEntity(null));
        assertNull(transactionCategoryMapper.toDto(null));
        assertNull(transactionCategoryMapper.toEntity(null));
    }
}
