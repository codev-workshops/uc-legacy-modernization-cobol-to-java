package com.carddemo.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DtoJsonSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    static Stream<Object> allDtos() {
        return Stream.of(
                AccountDto.builder()
                        .acctId(12345678901L)
                        .acctActiveStatus("Y")
                        .acctCurrBal(new BigDecimal("1000.50"))
                        .acctCreditLimit(new BigDecimal("5000.00"))
                        .acctCashCreditLimit(new BigDecimal("2000.00"))
                        .acctOpenDate("2020-01-15")
                        .acctExpirationDate("2025-01-15")
                        .acctReissueDate("2023-01-15")
                        .acctCurrCycCredit(new BigDecimal("500.00"))
                        .acctCurrCycDebit(new BigDecimal("200.00"))
                        .acctAddrZip("90210")
                        .acctGroupId("GRP001")
                        .build(),

                CustomerDto.builder()
                        .custId(123456789L)
                        .custFirstName("John")
                        .custMiddleName("M")
                        .custLastName("Doe")
                        .custAddrLine1("123 Main St")
                        .custAddrLine2("Apt 4")
                        .custAddrLine3("")
                        .custAddrStateCd("CA")
                        .custAddrCountryCd("USA")
                        .custAddrZip("90210")
                        .custPhoneNum1("555-1234")
                        .custPhoneNum2("555-5678")
                        .custSsn(123456789L)
                        .custGovtIssuedId("DL12345")
                        .custDob("1990-01-15")
                        .custEftAccountId("EFT001")
                        .custPriCardHolderInd("Y")
                        .custFicoCreditScore(750)
                        .build(),

                CardDto.builder()
                        .cardNum("4111111111111111")
                        .cardAcctId(12345678901L)
                        .cardCvvCd(123)
                        .cardEmbossedName("JOHN DOE")
                        .cardExpirationDate("2025-12-31")
                        .cardActiveStatus("Y")
                        .build(),

                CardXrefDto.builder()
                        .xrefCardNum("4111111111111111")
                        .xrefCustId(123456789L)
                        .xrefAcctId(12345678901L)
                        .build(),

                TransactionDto.builder()
                        .tranId("TRN0000000000001")
                        .tranTypeCd("SA")
                        .tranCatCd(5001)
                        .tranSource("ONLINE")
                        .tranDesc("Purchase at Store")
                        .tranAmt(new BigDecimal("99.99"))
                        .tranMerchantId(123456789L)
                        .tranMerchantName("Test Store")
                        .tranMerchantCity("New York")
                        .tranMerchantZip("10001")
                        .tranCardNum("4111111111111111")
                        .tranOrigTs("2024-01-15T10:30:00.000000")
                        .tranProcTs("2024-01-15T10:30:05.000000")
                        .build(),

                DailyTransactionDto.builder()
                        .dalytranId("DTR0000000000001")
                        .dalytranTypeCd("SA")
                        .dalytranCatCd(5001)
                        .dalytranSource("ONLINE")
                        .dalytranDesc("Daily Purchase")
                        .dalytranAmt(new BigDecimal("50.00"))
                        .dalytranMerchantId(123456789L)
                        .dalytranMerchantName("Daily Store")
                        .dalytranMerchantCity("Boston")
                        .dalytranMerchantZip("02101")
                        .dalytranCardNum("4111111111111111")
                        .dalytranOrigTs("2024-01-15T10:30:00.000000")
                        .dalytranProcTs("2024-01-15T10:30:05.000000")
                        .build(),

                TranCatBalanceDto.builder()
                        .trancatAcctId(12345678901L)
                        .trancatTypeCd("SA")
                        .trancatCd(5001)
                        .tranCatBal(new BigDecimal("1500.75"))
                        .build(),

                DisclosureGroupDto.builder()
                        .disAcctGroupId("GRP001")
                        .disTranTypeCd("SA")
                        .disTranCatCd(5001)
                        .disIntRate(new BigDecimal("12.50"))
                        .build(),

                TransactionTypeDto.builder()
                        .tranType("SA")
                        .tranTypeDesc("Sale")
                        .build(),

                TransactionCategoryDto.builder()
                        .tranTypeCd("SA")
                        .tranCatCd(5001)
                        .tranCatTypeDesc("Retail Purchase")
                        .build(),

                UserDto.builder()
                        .userId("USER0001")
                        .firstName("John")
                        .lastName("Doe")
                        .password("hashed01")
                        .userType("A")
                        .build(),

                ExportRecordDto.builder()
                        .recordType("ACCOUNT")
                        .recordData("some data")
                        .build(),

                TransactionReportDto.builder()
                        .tranReportTransId("TRN001")
                        .tranReportAccountId("ACCT001")
                        .tranReportTypeCd("SA")
                        .tranReportTypeDesc("Sale")
                        .tranReportCatCd(5001)
                        .tranReportCatDesc("Retail")
                        .tranReportSource("ONLINE")
                        .tranReportAmt(new BigDecimal("250.00"))
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("allDtos")
    void serializeAndDeserialize(Object dto) throws Exception {
        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).isNotBlank();

        Object deserialized = objectMapper.readValue(json, dto.getClass());
        assertThat(deserialized).isEqualTo(dto);
    }

    @ParameterizedTest
    @MethodSource("allDtos")
    void jsonContainsExpectedStructure(Object dto) throws Exception {
        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).startsWith("{");
        assertThat(json).endsWith("}");
    }
}
