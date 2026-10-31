package com.carddemo.batch.cbact01c;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FileStatusTest {

  @ParameterizedTest
  @CsvSource({"35, 0035", "00, 0000", "10, 0010", "9A, 9065"})
  void formatsLike9910DisplayIoStatus(String status, String expected) {
    assertEquals(expected, FileStatus.format(status));
  }

  @Test
  void nonNumericSecondByteIsShownAsItsBinaryValue() {
    assertEquals("9032", FileStatus.format("9 "));
  }
}
