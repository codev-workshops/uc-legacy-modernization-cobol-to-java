package com.carddemo.account.mapper;

import com.carddemo.account.entity.Customer;
import com.carddemo.common.dto.CustomerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMapperTest {

    private CustomerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CustomerMapper.class);
    }

    @Test
    void testToDto() {
        Customer entity = Customer.builder()
                .custId(123456789L)
                .custFirstName("John")
                .custMiddleName("M")
                .custLastName("Doe")
                .custAddrLine1("123 Main St")
                .custAddrLine2("Apt 4")
                .custAddrLine3("")
                .custAddrStateCd("NY")
                .custAddrCountryCd("USA")
                .custAddrZip("10001")
                .custPhoneNum1("555-123-4567")
                .custPhoneNum2("555-987-6543")
                .custSsn(987654321L)
                .custGovtIssuedId("DL12345")
                .custDob("1990-05-15")
                .custEftAccountId("EFT001")
                .custPriCardHolderInd("Y")
                .custFicoCreditScore(750)
                .build();

        CustomerDto dto = mapper.toDto(entity);

        assertEquals(entity.getCustId(), dto.getCustId());
        assertEquals(entity.getCustFirstName(), dto.getCustFirstName());
        assertEquals(entity.getCustMiddleName(), dto.getCustMiddleName());
        assertEquals(entity.getCustLastName(), dto.getCustLastName());
        assertEquals(entity.getCustAddrLine1(), dto.getCustAddrLine1());
        assertEquals(entity.getCustAddrLine2(), dto.getCustAddrLine2());
        assertEquals(entity.getCustAddrLine3(), dto.getCustAddrLine3());
        assertEquals(entity.getCustAddrStateCd(), dto.getCustAddrStateCd());
        assertEquals(entity.getCustAddrCountryCd(), dto.getCustAddrCountryCd());
        assertEquals(entity.getCustAddrZip(), dto.getCustAddrZip());
        assertEquals(entity.getCustPhoneNum1(), dto.getCustPhoneNum1());
        assertEquals(entity.getCustPhoneNum2(), dto.getCustPhoneNum2());
        assertEquals(entity.getCustSsn(), dto.getCustSsn());
        assertEquals(entity.getCustGovtIssuedId(), dto.getCustGovtIssuedId());
        assertEquals(entity.getCustDob(), dto.getCustDob());
        assertEquals(entity.getCustEftAccountId(), dto.getCustEftAccountId());
        assertEquals(entity.getCustPriCardHolderInd(), dto.getCustPriCardHolderInd());
        assertEquals(entity.getCustFicoCreditScore(), dto.getCustFicoCreditScore());
    }

    @Test
    void testToEntity() {
        CustomerDto dto = CustomerDto.builder()
                .custId(1L)
                .custFirstName("Jane")
                .custLastName("Smith")
                .build();

        Customer entity = mapper.toEntity(dto);

        assertEquals(dto.getCustId(), entity.getCustId());
        assertEquals(dto.getCustFirstName(), entity.getCustFirstName());
        assertEquals(dto.getCustLastName(), entity.getCustLastName());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void testNullMapping() {
        assertNull(mapper.toDto(null));
        assertNull(mapper.toEntity(null));
    }
}
