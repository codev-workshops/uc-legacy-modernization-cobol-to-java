package com.carddemo.batch.export;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecordTypeTest {

    @ParameterizedTest
    @CsvSource({
            "C, CUSTOMER",
            "A, ACCOUNT",
            "X, CARD_XREF",
            "T, TRANSACTION",
            "D, CARD",
            "B, TRAN_CAT_BALANCE"
    })
    void fromCode_shouldReturnCorrectType(char code, String expectedName) {
        assertEquals(RecordType.valueOf(expectedName), RecordType.fromCode(code));
    }

    @Test
    void fromCode_unknownCode_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> RecordType.fromCode('Z'));
    }

    @ParameterizedTest
    @CsvSource({
            "CUSTOMER, C",
            "ACCOUNT, A",
            "CARD_XREF, X",
            "TRANSACTION, T",
            "CARD, D",
            "TRAN_CAT_BALANCE, B"
    })
    void getCode_shouldReturnCorrectChar(String name, char expectedCode) {
        assertEquals(expectedCode, RecordType.valueOf(name).getCode());
    }
}
