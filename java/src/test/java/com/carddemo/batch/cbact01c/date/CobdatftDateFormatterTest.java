package com.carddemo.batch.cbact01c.date;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CobdatftDateFormatterTest {

    private static final String FILL12 = String.valueOf(CobdatftDateFormatter.FILL).repeat(12);
    private static final String FILL10 = String.valueOf(CobdatftDateFormatter.FILL).repeat(10);
    private static final String FILL20 = String.valueOf(CobdatftDateFormatter.FILL).repeat(20);
    private static final String SPACES38 = " ".repeat(38);
    private static final String INVALID_INPUT38 = "INVALID INPUT" + " ".repeat(25);

    private final DateFormatter formatter = new CobdatftDateFormatter();

    private DateConversionResponse convert(char inType, char outType, String date) {
        return formatter.convert(new DateConversionRequest(inType, outType, date));
    }

    private static void assertNoError(DateConversionResponse r) {
        assertFalse(r.isError());
        assertEquals(SPACES38, r.errorMessage());
    }

    private static void assertInvalidInput(DateConversionResponse r) {
        assertAll(
                () -> assertTrue(r.isError()),
                () -> assertEquals(INVALID_INPUT38, r.errorMessage()),
                () -> assertEquals(FILL20, r.outputDate()),
                () -> assertEquals(FILL10, r.outputDate10()));
    }

    @Nested
    class Type2YyyyMmDdInput {

        @Test
        void type2ToType2ReformatsToYyyymmdd() {
            DateConversionResponse r = convert('2', '2', "2025-05-20");
            assertEquals("20250520  ", r.outputDate10());
            assertEquals("20250520" + FILL12, r.outputDate());
            assertNoError(r);
        }

        @Test
        void separatorsAreNotValidated() {
            DateConversionResponse r = convert('2', '2', "2025/05/20");
            assertEquals("20250520  ", r.outputDate10());
            assertNoError(r);
        }

        @Test
        void calendarIsNotValidated() {
            DateConversionResponse r = convert('2', '2', "2023-13-45");
            assertEquals("20231345  ", r.outputDate10());
            assertNoError(r);
        }

        @Test
        void nonDigitsArePassedThroughPositionally() {
            DateConversionResponse r = convert('2', '2', "ABCD-EF-GH");
            assertEquals("ABCDEFGH  ", r.outputDate10());
            assertNoError(r);
        }

        @Test
        void bytesBeyondPosition9AreIgnored() {
            DateConversionResponse r = convert('2', '2', "2025-05-20XXXXXXXXXX");
            assertEquals("20250520" + FILL12, r.outputDate());
            assertNoError(r);
        }

        @Test
        void outType1IsAnError() {
            assertInvalidInput(convert('2', '1', "2025-05-20"));
        }

        @ParameterizedTest
        @ValueSource(chars = {' ', '9', 'X', '2', '\0'})
        void anyOutTypeOtherThan1BehavesLikeType2(char outType) {
            DateConversionResponse r = convert('2', outType, "2025-05-20");
            assertEquals("20250520" + FILL12, r.outputDate());
            assertNoError(r);
        }
    }

    @Nested
    class Type1YyyymmddInput {

        @Test
        void type1ToType1InsertsSeparators() {
            DateConversionResponse r = convert('1', '1', "20250520");
            assertEquals("2025-05-20", r.outputDate10());
            assertEquals("2025-05-20" + FILL10, r.outputDate());
            assertNoError(r);
        }

        @Test
        void outType2IsAnError() {
            assertInvalidInput(convert('1', '2', "20250520"));
        }

        @Test
        void dashAtPosition4IsAnError() {
            assertInvalidInput(convert('1', '1', "2025-05-20"));
        }

        @Test
        void dashAtPosition4IsCheckedBeforeOutType() {
            assertInvalidInput(convert('1', 'X', "2025-05-20"));
        }

        @ParameterizedTest
        @ValueSource(chars = {'X', ' ', '9', '1', '\0'})
        void anyOutTypeOtherThan2BehavesLikeType1(char outType) {
            DateConversionResponse r = convert('1', outType, "20250520");
            assertEquals("2025-05-20" + FILL10, r.outputDate());
            assertNoError(r);
        }

        @Test
        void nonDigitsArePassedThroughPositionally() {
            DateConversionResponse r = convert('1', '1', "ABCDEFGH");
            assertEquals("ABCD-EF-GH", r.outputDate10());
            assertNoError(r);
        }

        @Test
        void otherSeparatorsAtPosition4AreNotRejected() {
            DateConversionResponse r = convert('1', '1', "2025/05/20");
            assertEquals("2025-/0-5/", r.outputDate10());
            assertNoError(r);
        }
    }

    @Nested
    class UnsupportedInputType {

        @ParameterizedTest
        @ValueSource(chars = {'3', ' ', '0', 'A', '\0'})
        void isAnErrorRegardlessOfOutType(char inType) {
            assertInvalidInput(convert(inType, '1', "20250520"));
            assertInvalidInput(convert(inType, '2', "2025-05-20"));
            assertInvalidInput(convert(inType, ' ', "2025-05-20"));
        }
    }

    @Nested
    class RequestPadding {

        @Test
        void shortInputIsSpacePaddedTo20() {
            DateConversionRequest r = new DateConversionRequest('2', '2', "2025-05-20");
            assertEquals(20, r.inputDate().length());
            assertEquals("2025-05-20          ", r.inputDate());
        }

        @Test
        void longInputIsTruncatedTo20() {
            DateConversionRequest r = new DateConversionRequest('2', '2', "2025-05-20ABCDEFGHIJKLMNOP");
            assertEquals("2025-05-20ABCDEFGHIJ", r.inputDate());
        }

        @Test
        void exactInputIsUnchanged() {
            String exact = "2025-05-20ABCDEFGHIJ";
            assertEquals(exact, new DateConversionRequest('2', '2', exact).inputDate());
        }

        @ParameterizedTest
        @CsvSource(value = {"''", "NULL"}, nullValues = "NULL")
        void emptyOrNullInputIsAllSpaces(String input) {
            assertEquals(" ".repeat(20), new DateConversionRequest('2', '2', input).inputDate());
        }

        @Test
        void emptyInputStillConvertsWithoutError() {
            DateConversionResponse r = convert('2', '2', "");
            assertEquals(" ".repeat(8) + FILL12, r.outputDate());
            assertNoError(r);
        }
    }

    @Nested
    class ResponseInvariants {

        @ParameterizedTest
        @CsvSource({
                "1, 1, 20250520",
                "1, 2, 20250520",
                "2, 2, 2025-05-20",
                "2, 1, 2025-05-20",
                "3, 1, 20250520",
                "2, 2, ''",
        })
        void lengthsAreAlwaysFixed(char inType, char outType, String date) {
            DateConversionResponse r = convert(inType, outType, date);
            assertEquals(20, r.outputDate().length());
            assertEquals(38, r.errorMessage().length());
            assertEquals(10, r.outputDate10().length());
        }

        @Test
        void constructorNormalisesComponents() {
            DateConversionResponse r = new DateConversionResponse("abc", "INVALID INPUT");
            assertEquals("abc" + " ".repeat(17), r.outputDate());
            assertEquals(INVALID_INPUT38, r.errorMessage());
            assertTrue(r.isError());

            DateConversionResponse tooLong = new DateConversionResponse("x".repeat(30), "y".repeat(50));
            assertEquals(20, tooLong.outputDate().length());
            assertEquals(38, tooLong.errorMessage().length());
        }

        @Test
        void isErrorIsFalseOnlyForAllSpaces() {
            assertFalse(new DateConversionResponse("", "").isError());
            assertFalse(new DateConversionResponse("", SPACES38).isError());
            assertTrue(new DateConversionResponse("", " ".repeat(37) + "x").isError());
        }
    }
}
