package com.carddemo.batch.cbact01c;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DateFormatConverterTest {

  private static final String BLANK = " ".repeat(20);

  @Test
  void isoToCompactAsCalledByCbact01c() {
    DateFormatConverter.Result result = DateFormatConverter.convert('2', "2025-05-20", '2', BLANK);
    assertFalse(result.isError());
    assertEquals("20250520" + " ".repeat(12), result.outputDate());
  }

  @Test
  void compactToIso() {
    DateFormatConverter.Result result = DateFormatConverter.convert('1', "20250520", '1', BLANK);
    assertFalse(result.isError());
    assertEquals("2025-05-20" + " ".repeat(10), result.outputDate());
  }

  @Test
  void onlyMovesCharactersWithoutValidating() {
    assertEquals(
        "XXXXYYZZ", DateFormatConverter.convert('2', "XXXX/YY/ZZ", '2', BLANK).outputDate().trim());
    assertEquals(
        "202723", DateFormatConverter.convert('2', "20271231", '2', BLANK).outputDate().trim());
  }

  @Test
  void leavesTheRestOfTheBufferUntouched() {
    String previous = "ABCDEFGHIJKLMNOPQRST";
    assertEquals(
        "20250520IJKLMNOPQRST",
        DateFormatConverter.convert('2', "2025-05-20", '2', previous).outputDate());
  }

  @ParameterizedTest
  @CsvSource({"2, 2025-05-20, 1", "1, 20250520, 2", "1, 2025-05-20, 1", "3, 2025-05-20, 2"})
  void rejectsUnsupportedCombinations(char in, String date, char out) {
    String previous = "PREVIOUS            ";
    DateFormatConverter.Result result = DateFormatConverter.convert(in, date, out, previous);
    assertTrue(result.isError());
    assertEquals(DateFormatConverter.INVALID_INPUT, result.errorMessage().trim());
    assertEquals(previous, result.outputDate());
  }
}
