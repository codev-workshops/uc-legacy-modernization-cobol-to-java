package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayRecord;
import com.carddemo.batch.cbact01c.model.ArraySlot;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.model.VbRecord1;
import com.carddemo.batch.cbact01c.model.VbRecord2;
import com.carddemo.batch.cbact01c.service.AccountFileProcessor;
import com.carddemo.batch.cbact01c.service.AccountFileProcessor.ProcessingResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccountFileProcessorTest {

    private AccountFileProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new AccountFileProcessor();
    }

    // --- Sample account builders ---

    private AccountRecord activeAccountWithDebit() {
        return new AccountRecord(
                "00000000001", "Y",
                new BigDecimal("5000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("3000.00"),
                "2020-01-15", "2025-12-31", "2024-06-15",
                new BigDecimal("1000.00"),
                new BigDecimal("500.00"),
                "1234567890", "PLATINUM  "
        );
    }

    private AccountRecord activeAccountZeroDebit() {
        return new AccountRecord(
                "00000000002", "Y",
                new BigDecimal("0.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("1000.00"),
                "2021-03-20", "2026-03-20", "2023-11-01",
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                "9876543210", "GOLD      "
        );
    }

    private AccountRecord inactiveAccountNegativeBalance() {
        return new AccountRecord(
                "00000000003", "N",
                new BigDecimal("-200.50"),
                new BigDecimal("2000.00"),
                new BigDecimal("500.00"),
                "2019-07-04", "2024-07-04", "2022-02-28",
                new BigDecimal("50.00"),
                new BigDecimal("250.50"),
                "5555555555", "SILVER    "
        );
    }

    // ===============================================================
    // 1. Date conversion tests
    // ===============================================================

    @Nested
    @DisplayName("Date conversion (replaces COBDATFT assembler call)")
    class DateConversionTests {

        @Test
        @DisplayName("Standard date: 2024-01-15 -> 20240115")
        void testStandardDate() {
            assertThat(processor.convertDateFormat("2024-01-15")).isEqualTo("20240115");
        }

        @Test
        @DisplayName("Year-end date: 2023-12-31 -> 20231231")
        void testYearEndDate() {
            assertThat(processor.convertDateFormat("2023-12-31")).isEqualTo("20231231");
        }

        @Test
        @DisplayName("Leap year date: 2000-02-29 -> 20000229")
        void testLeapYearDate() {
            assertThat(processor.convertDateFormat("2000-02-29")).isEqualTo("20000229");
        }

        @Test
        @DisplayName("Null input returns empty string")
        void testNullDate() {
            assertThat(processor.convertDateFormat(null)).isEqualTo("");
        }

        @Test
        @DisplayName("Already compact date passes through")
        void testAlreadyCompact() {
            assertThat(processor.convertDateFormat("20240115")).isEqualTo("20240115");
        }
    }

    // ===============================================================
    // 2. Debit substitution tests
    // ===============================================================

    @Nested
    @DisplayName("Debit substitution business rule")
    class DebitSubstitutionTests {

        @Test
        @DisplayName("Zero debit -> 2525.00")
        void testZeroDebitSubstitution() {
            AccountRecord input = activeAccountZeroDebit();
            OutAccountRecord out = processor.buildOutRecord(input);

            assertThat(out.getCurrCycDebit())
                    .isEqualByComparingTo(new BigDecimal("2525.00"));
        }

        @Test
        @DisplayName("Non-zero debit (500.00) -> unchanged")
        void testNonZeroDebitUnchanged() {
            AccountRecord input = activeAccountWithDebit();
            OutAccountRecord out = processor.buildOutRecord(input);

            assertThat(out.getCurrCycDebit())
                    .isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Non-zero debit (250.50) -> unchanged")
        void testNonZeroDebitFractional() {
            AccountRecord input = inactiveAccountNegativeBalance();
            OutAccountRecord out = processor.buildOutRecord(input);

            assertThat(out.getCurrCycDebit())
                    .isEqualByComparingTo(new BigDecimal("250.50"));
        }
    }

    // ===============================================================
    // 3. Array record population tests
    // ===============================================================

    @Nested
    @DisplayName("Array record population (ARRYFILE)")
    class ArrayRecordTests {

        @Test
        @DisplayName("Correct 5-slot population with balance 5000.00")
        void testArrayRecordPopulation() {
            AccountRecord input = activeAccountWithDebit();
            ArrayRecord arr = processor.buildArrayRecord(input);

            assertThat(arr.getAcctId()).isEqualTo("00000000001");
            List<ArraySlot> slots = arr.getSlots();
            assertThat(slots).hasSize(5);

            // Slot 0: actual balance, 1005.00
            assertThat(slots.get(0).getBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(slots.get(0).getCycDebit()).isEqualByComparingTo(new BigDecimal("1005.00"));

            // Slot 1: actual balance, 1525.00
            assertThat(slots.get(1).getBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(slots.get(1).getCycDebit()).isEqualByComparingTo(new BigDecimal("1525.00"));

            // Slot 2: -1025.00, -2500.00
            assertThat(slots.get(2).getBalance()).isEqualByComparingTo(new BigDecimal("-1025.00"));
            assertThat(slots.get(2).getCycDebit()).isEqualByComparingTo(new BigDecimal("-2500.00"));

            // Slot 3: 0, 0
            assertThat(slots.get(3).getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(slots.get(3).getCycDebit()).isEqualByComparingTo(BigDecimal.ZERO);

            // Slot 4: 0, 0
            assertThat(slots.get(4).getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(slots.get(4).getCycDebit()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Negative balance propagates to slots 0 and 1")
        void testArrayWithNegativeBalance() {
            AccountRecord input = inactiveAccountNegativeBalance();
            ArrayRecord arr = processor.buildArrayRecord(input);

            assertThat(arr.getSlots().get(0).getBalance())
                    .isEqualByComparingTo(new BigDecimal("-200.50"));
            assertThat(arr.getSlots().get(1).getBalance())
                    .isEqualByComparingTo(new BigDecimal("-200.50"));
        }

        @Test
        @DisplayName("Zero balance propagates correctly")
        void testArrayWithZeroBalance() {
            AccountRecord input = activeAccountZeroDebit();
            ArrayRecord arr = processor.buildArrayRecord(input);

            assertThat(arr.getSlots().get(0).getBalance())
                    .isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(arr.getSlots().get(1).getBalance())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ===============================================================
    // 4. VB record population tests
    // ===============================================================

    @Nested
    @DisplayName("Variable-block record population (VBRCFILE)")
    class VbRecordTests {

        @Test
        @DisplayName("VB1 has correct account ID and status")
        void testVbRecord1() {
            AccountRecord input = activeAccountWithDebit();
            VbRecord1 vb1 = processor.buildVbRecord1(input);

            assertThat(vb1.getAcctId()).isEqualTo("00000000001");
            assertThat(vb1.getActiveStatus()).isEqualTo("Y");
        }

        @Test
        @DisplayName("VB1 inactive account")
        void testVbRecord1Inactive() {
            AccountRecord input = inactiveAccountNegativeBalance();
            VbRecord1 vb1 = processor.buildVbRecord1(input);

            assertThat(vb1.getAcctId()).isEqualTo("00000000003");
            assertThat(vb1.getActiveStatus()).isEqualTo("N");
        }

        @Test
        @DisplayName("VB2 has correct ID, balance, credit limit, and reissue year")
        void testVbRecord2() {
            AccountRecord input = activeAccountWithDebit();
            VbRecord2 vb2 = processor.buildVbRecord2(input);

            assertThat(vb2.getAcctId()).isEqualTo("00000000001");
            assertThat(vb2.getCurrBal()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertThat(vb2.getCreditLimit()).isEqualByComparingTo(new BigDecimal("10000.00"));
            assertThat(vb2.getReissueYear()).isEqualTo("2024");
        }

        @Test
        @DisplayName("VB2 extracts reissue year correctly for 2022-02-28")
        void testVbRecord2ReissueYear() {
            AccountRecord input = inactiveAccountNegativeBalance();
            VbRecord2 vb2 = processor.buildVbRecord2(input);

            assertThat(vb2.getReissueYear()).isEqualTo("2022");
        }

        @Test
        @DisplayName("VB2 handles negative balance")
        void testVbRecord2NegativeBalance() {
            AccountRecord input = inactiveAccountNegativeBalance();
            VbRecord2 vb2 = processor.buildVbRecord2(input);

            assertThat(vb2.getCurrBal()).isEqualByComparingTo(new BigDecimal("-200.50"));
            assertThat(vb2.getCreditLimit()).isEqualByComparingTo(new BigDecimal("2000.00"));
        }
    }

    // ===============================================================
    // 5. Full end-to-end processing test
    // ===============================================================

    @Nested
    @DisplayName("Full processing (end-to-end)")
    class FullProcessingTests {

        @Test
        @DisplayName("Process 3 sample accounts — verify all output sets")
        void testFullProcessing() {
            List<AccountRecord> inputs = List.of(
                    activeAccountWithDebit(),
                    activeAccountZeroDebit(),
                    inactiveAccountNegativeBalance()
            );

            ProcessingResult result = processor.process(inputs);

            // Correct counts
            assertThat(result.getOutRecords()).hasSize(3);
            assertThat(result.getArrayRecords()).hasSize(3);
            assertThat(result.getVbRecords1()).hasSize(3);
            assertThat(result.getVbRecords2()).hasSize(3);

            // Verify account 1 output record
            OutAccountRecord out1 = result.getOutRecords().get(0);
            assertThat(out1.getAcctId()).isEqualTo("00000000001");
            assertThat(out1.getReissueDate()).isEqualTo("20240615");
            assertThat(out1.getCurrCycDebit()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(out1.getCurrBal()).isEqualByComparingTo(new BigDecimal("5000.00"));

            // Verify account 2 output record (zero debit -> 2525.00)
            OutAccountRecord out2 = result.getOutRecords().get(1);
            assertThat(out2.getAcctId()).isEqualTo("00000000002");
            assertThat(out2.getCurrCycDebit()).isEqualByComparingTo(new BigDecimal("2525.00"));
            assertThat(out2.getReissueDate()).isEqualTo("20231101");

            // Verify account 3 output record (negative balance, non-zero debit)
            OutAccountRecord out3 = result.getOutRecords().get(2);
            assertThat(out3.getAcctId()).isEqualTo("00000000003");
            assertThat(out3.getCurrBal()).isEqualByComparingTo(new BigDecimal("-200.50"));
            assertThat(out3.getCurrCycDebit()).isEqualByComparingTo(new BigDecimal("250.50"));
            assertThat(out3.getActiveStatus()).isEqualTo("N");

            // Verify array records
            ArrayRecord arr2 = result.getArrayRecords().get(1);
            assertThat(arr2.getAcctId()).isEqualTo("00000000002");
            assertThat(arr2.getSlots().get(0).getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

            // Verify VB1/VB2 pairing
            VbRecord1 vb1_3 = result.getVbRecords1().get(2);
            VbRecord2 vb2_3 = result.getVbRecords2().get(2);
            assertThat(vb1_3.getAcctId()).isEqualTo("00000000003");
            assertThat(vb1_3.getActiveStatus()).isEqualTo("N");
            assertThat(vb2_3.getAcctId()).isEqualTo("00000000003");
            assertThat(vb2_3.getReissueYear()).isEqualTo("2022");
        }

        @Test
        @DisplayName("Output record preserves open/expiration dates unchanged")
        void testDatesPreserved() {
            AccountRecord input = activeAccountWithDebit();
            ProcessingResult result = processor.process(List.of(input));

            OutAccountRecord out = result.getOutRecords().get(0);
            assertThat(out.getOpenDate()).isEqualTo("2020-01-15");
            assertThat(out.getExpirationDate()).isEqualTo("2025-12-31");
        }
    }

    // ===============================================================
    // 6. Empty input test
    // ===============================================================

    @Test
    @DisplayName("Empty input -> empty output lists")
    void testEmptyInput() {
        ProcessingResult result = processor.process(Collections.emptyList());

        assertThat(result.getOutRecords()).isEmpty();
        assertThat(result.getArrayRecords()).isEmpty();
        assertThat(result.getVbRecords1()).isEmpty();
        assertThat(result.getVbRecords2()).isEmpty();
    }

    // ===============================================================
    // 7. Multiple records test
    // ===============================================================

    @Test
    @DisplayName("Process 3+ records — correct count and per-record correctness")
    void testMultipleRecords() {
        AccountRecord extra = new AccountRecord(
                "00000000004", "Y",
                new BigDecimal("12345.67"),
                new BigDecimal("50000.00"),
                new BigDecimal("10000.00"),
                "2022-06-01", "2027-06-01", "2025-01-15",
                new BigDecimal("2000.00"),
                new BigDecimal("750.25"),
                "1111111111", "DIAMOND   "
        );

        List<AccountRecord> inputs = List.of(
                activeAccountWithDebit(),
                activeAccountZeroDebit(),
                inactiveAccountNegativeBalance(),
                extra
        );

        ProcessingResult result = processor.process(inputs);

        assertThat(result.getOutRecords()).hasSize(4);
        assertThat(result.getArrayRecords()).hasSize(4);
        assertThat(result.getVbRecords1()).hasSize(4);
        assertThat(result.getVbRecords2()).hasSize(4);

        // Verify the 4th record
        OutAccountRecord out4 = result.getOutRecords().get(3);
        assertThat(out4.getAcctId()).isEqualTo("00000000004");
        assertThat(out4.getCurrCycDebit()).isEqualByComparingTo(new BigDecimal("750.25"));
        assertThat(out4.getReissueDate()).isEqualTo("20250115");
        assertThat(out4.getCurrBal()).isEqualByComparingTo(new BigDecimal("12345.67"));

        ArrayRecord arr4 = result.getArrayRecords().get(3);
        assertThat(arr4.getSlots().get(0).getBalance()).isEqualByComparingTo(new BigDecimal("12345.67"));
        assertThat(arr4.getSlots().get(1).getBalance()).isEqualByComparingTo(new BigDecimal("12345.67"));

        VbRecord2 vb2_4 = result.getVbRecords2().get(3);
        assertThat(vb2_4.getReissueYear()).isEqualTo("2025");
        assertThat(vb2_4.getCreditLimit()).isEqualByComparingTo(new BigDecimal("50000.00"));
    }

    // ===============================================================
    // Additional edge case tests
    // ===============================================================

    @Nested
    @DisplayName("BigDecimal scale and rounding (RISK-01 compliance)")
    class ScaleTests {

        @Test
        @DisplayName("Values with extra precision are truncated (RoundingMode.DOWN)")
        void testTruncation() {
            AccountRecord input = new AccountRecord(
                    "00000000005", "Y",
                    new BigDecimal("1234.5678"),  // extra precision
                    new BigDecimal("5000.999"),
                    new BigDecimal("1000.001"),
                    "2024-03-15", "2029-03-15", "2028-01-01",
                    new BigDecimal("100.999"),
                    new BigDecimal("50.555"),
                    "0000000000", "TEST      "
            );

            OutAccountRecord out = processor.buildOutRecord(input);

            assertThat(out.getCurrBal()).isEqualByComparingTo(new BigDecimal("1234.56"));
            assertThat(out.getCreditLimit()).isEqualByComparingTo(new BigDecimal("5000.99"));
            assertThat(out.getCashCreditLimit()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(out.getCurrCycCredit()).isEqualByComparingTo(new BigDecimal("100.99"));
            assertThat(out.getCurrCycDebit()).isEqualByComparingTo(new BigDecimal("50.55"));
        }
    }
}
