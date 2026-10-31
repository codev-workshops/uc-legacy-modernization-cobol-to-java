package com.carddemo.batch.cbact01c;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CodecTest {

  @ParameterizedTest
  @CsvSource({
    "00000001940{, 194.00, false",
    "00000000099R, -9.99, true",
    "00000012345E, 1234.55, false",
    "99999999999I, 9999999999.99, false",
    "00000000000}, 0.00, true",
    "000000100000, 1000.00, false"
  })
  void decodesOverpunchedZonedDecimal(String field, String value, boolean negative) {
    SignedAmount amount = ZonedDecimal.decode(field, 2);
    assertEquals(new BigDecimal(value), amount.value());
    assertEquals(negative, amount.negative());
  }

  @ParameterizedTest
  @CsvSource({
    "194.00, 00000001940{",
    "-1025.00, 00000010250}",
    "-0.99, 00000000009R",
    "0, 00000000000{",
    "12345678901.23, 23456789012C"
  })
  void encodesZonedDecimalLikeMove(String value, String field) {
    assertEquals(field, ZonedDecimal.encode(SignedAmount.of(value), 12, 2));
  }

  @Test
  void keepsNegativeZero() {
    SignedAmount negativeZero = ZonedDecimal.decode("00000000000}", 2);
    assertTrue(negativeZero.isZero());
    assertEquals("00000000000}", ZonedDecimal.encode(negativeZero, 12, 2));
  }

  @Test
  void rejectsInvalidZonedDecimal() {
    assertThrows(IllegalArgumentException.class, () -> ZonedDecimal.decode("0000000 940{", 2));
    assertThrows(IllegalArgumentException.class, () -> ZonedDecimal.decode("00000001940*", 2));
    assertThrows(IllegalArgumentException.class, () -> ZonedDecimal.decode("", 2));
  }

  @Test
  void rejectsInconsistentSign() {
    BigDecimal one = BigDecimal.ONE;
    assertThrows(IllegalArgumentException.class, () -> new SignedAmount(one, true));
  }

  @ParameterizedTest
  @CsvSource({
    "2525.00, 0000000252500C",
    "1005.00, 0000000100500C",
    "-2500.00, 0000000250000D",
    "0, 0000000000000C",
    "-0.01, 0000000000001D"
  })
  void encodesPackedDecimal(String value, String hex) {
    byte[] packed = PackedDecimal.encode(new BigDecimal(value), 12, 2);
    assertEquals(7, packed.length);
    assertEquals(hex, HexFormat.of().withUpperCase().formatHex(packed));
    assertEquals(new BigDecimal(value).setScale(2), PackedDecimal.decode(packed, 2));
  }

  @Test
  void rejectsInvalidPackedDecimal() {
    assertThrows(IllegalArgumentException.class, () -> PackedDecimal.decode(new byte[7], 2));
    byte[] badDigit = HexFormat.of().parseHex("00000000000A0C");
    assertThrows(IllegalArgumentException.class, () -> PackedDecimal.decode(badDigit, 2));
  }

  @Test
  void unsignedPackedReadsPositive() {
    byte[] packed = HexFormat.of().parseHex("0000000252500F");
    assertEquals(new BigDecimal("2525.00"), PackedDecimal.decode(packed, 2));
    assertArrayEquals(
        HexFormat.of().parseHex("0000000252500C"),
        PackedDecimal.encode(new BigDecimal("2525.00"), 12, 2));
  }
}
