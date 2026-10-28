package com.carddemo.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.harness.codec.AlphanumericCodec;
import com.carddemo.harness.codec.BinaryCodec;
import com.carddemo.harness.codec.CardDemoLayouts;
import com.carddemo.harness.codec.Pic;
import com.carddemo.harness.codec.PackedDecimalCodec;
import com.carddemo.harness.codec.RecordCodec;
import com.carddemo.harness.codec.SourceEncoding;
import com.carddemo.harness.codec.Undecodable;
import com.carddemo.harness.codec.ZonedDecimalCodec;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CodecTest {
  private final ZonedDecimalCodec zoned = new ZonedDecimalCodec();
  private final PackedDecimalCodec packed = new PackedDecimalCodec();
  private final BinaryCodec binary = new BinaryCodec();

  @Test
  void parsesSupportedPictures() {
    assertThat(Pic.parse("X(10)").intDigits()).isEqualTo(10);
    assertThat(Pic.parse("9(4)").intDigits()).isEqualTo(4);
    assertThat(Pic.parse("S9(10)V99")).satisfies(pic -> {
      assertThat(pic.signed()).isTrue();
      assertThat(pic.intDigits()).isEqualTo(10);
      assertThat(pic.scale()).isEqualTo(2);
    });
  }

  @Test
  void encodesAndDecodesEveryAsciiOverpunchCharacter() {
    String positive = "{ABCDEFGHI";
    String negative = "}JKLMNOPQR";
    for (int digit = 0; digit < 10; digit++) {
      byte[] positiveByte = {(byte) positive.charAt(digit)};
      byte[] negativeByte = {(byte) negative.charAt(digit)};
      assertThat(zoned.decode(positiveByte, Pic.parse("S9"), SourceEncoding.ASCII))
          .isEqualTo(Integer.toString(digit));
      assertThat(zoned.decode(negativeByte, Pic.parse("S9"), SourceEncoding.ASCII))
          .isEqualTo(digit == 0 ? Undecodable.of("ZONED_NEGATIVE_ZERO", negativeByte) : "-" + digit);
      assertThat(zoned.encode(digit + "", Pic.parse("S9"), SourceEncoding.ASCII))
          .containsExactly(positiveByte);
      if (digit != 0) {
        assertThat(zoned.encode("-" + digit, Pic.parse("S9"), SourceEncoding.ASCII))
            .containsExactly(negativeByte);
      }
    }
  }

  @Test
  void decodesEbcdicZonedSigns() {
    assertThat(zoned.decode(new byte[] {(byte) 0xf1, (byte) 0xf2, (byte) 0xc3},
        Pic.parse("S9(3)"), SourceEncoding.EBCDIC_CP037)).isEqualTo("123");
    assertThat(zoned.decode(new byte[] {(byte) 0xf1, (byte) 0xf2, (byte) 0xd3},
        Pic.parse("S9(3)"), SourceEncoding.EBCDIC_CP037)).isEqualTo("-123");
    assertThat(zoned.decode(new byte[] {(byte) 0xf1, (byte) 0xf2, (byte) 0xf3},
        Pic.parse("9(3)"), SourceEncoding.EBCDIC_CP037)).isEqualTo("123");
    assertThat(zoned.encode("-123", Pic.parse("S9(3)"), SourceEncoding.EBCDIC_CP037))
        .containsExactly((byte) 0xf1, (byte) 0xf2, (byte) 0xd3);
  }

  @Test
  void roundTripsPackedSignsAndRejectsInvalidNibblesAndNegativeZero() {
    assertThat(packed.decode(new byte[] {0x12, 0x3c}, Pic.parse("S9(3)"), SourceEncoding.ASCII))
        .isEqualTo("123");
    assertThat(packed.decode(new byte[] {0x12, 0x3d}, Pic.parse("S9(3)"), SourceEncoding.ASCII))
        .isEqualTo("-123");
    assertThat(packed.decode(new byte[] {0x12, 0x3f}, Pic.parse("9(3)"), SourceEncoding.ASCII))
        .isEqualTo("123");
    assertThat(packed.decode(new byte[] {0x12, 0x3c}, Pic.parse("9(3)"), SourceEncoding.ASCII))
        .isEqualTo(Undecodable.of("PACKED", new byte[] {0x12, 0x3c}));
    assertThat(packed.decode(new byte[] {0x1a, 0x3c}, Pic.parse("S9(3)"), SourceEncoding.ASCII))
        .isEqualTo(Undecodable.of("PACKED", new byte[] {0x1a, 0x3c}));
    assertThat(packed.decode(new byte[] {0x00, 0x0d}, Pic.parse("S9(3)"), SourceEncoding.ASCII))
        .isEqualTo(Undecodable.of("PACKED_NEGATIVE_ZERO", new byte[] {0x00, 0x0d}));
    assertThat(packed.encode("-123", Pic.parse("S9(3)"), SourceEncoding.ASCII))
        .containsExactly((byte) 0x12, (byte) 0x3d);
  }

  @Test
  void handlesBinaryWidthsSignsAndPicOverflow() {
    for (int digits : new int[] {4, 5, 10}) {
      Pic pic = Pic.parse("S9(" + digits + ")");
      byte[] encoded = binary.encode("-42", pic, SourceEncoding.ASCII);
      assertThat(encoded).hasSize(BinaryCodec.storageSize(digits));
      assertThat(binary.decode(encoded, pic, SourceEncoding.ASCII)).isEqualTo("-42");
    }
    assertThat(binary.decode(new byte[] {0x7f, (byte) 0xff}, Pic.parse("9(4)"),
        SourceEncoding.ASCII)).isEqualTo(Undecodable.of("BINARY", new byte[] {0x7f, (byte) 0xff}));
    assertThat(binary.decode(new byte[] {0, 42}, Pic.parse("9(4)"), SourceEncoding.ASCII))
        .isEqualTo("42");
    assertThat(binary.decode(new byte[] {0, 42}, Pic.parse("9(1)"), true)).isEqualTo("42");
  }

  @Test
  void recognizesUnprintableAlphaBytes() {
    AlphanumericCodec alpha = new AlphanumericCodec();
    byte[] lowValues = {0, 0, 0};
    assertThat(alpha.decode(lowValues, Pic.parse("X(3)"), SourceEncoding.ASCII))
        .isEqualTo(Undecodable.of("ALPHANUMERIC", lowValues));
  }

  @Test
  void exportCodecPreservesOccursFillerAndDiscriminatedBranches() throws Exception {
    byte[] customer = HarnessTestData.source("export").records().stream()
        .filter(record -> new String(new byte[] {record[0]}, SourceEncoding.EBCDIC_CP037.charset())
            .equals("C"))
        .findFirst().orElseThrow();
    RecordCodec codec = new RecordCodec(CardDemoLayouts.get("export"));
    Map<String, Object> decoded = codec.decode(customer, SourceEncoding.EBCDIC_CP037);
    assertThat(decoded).containsKey("EXPORT-CUSTOMER-DATA");
    @SuppressWarnings("unchecked")
    Map<String, Object> branch = (Map<String, Object>) decoded.get("EXPORT-CUSTOMER-DATA");
    assertThat(branch.get("EXP-CUST-ADDR-LINES")).as("OCCURS address lines").isInstanceOf(java.util.List.class);
    assertThat((java.util.List<?>) branch.get("EXP-CUST-ADDR-LINES")).hasSize(3);
    assertThat(branch).containsKey("FILLER");
    assertThat(codec.encode(decoded, SourceEncoding.EBCDIC_CP037)).containsExactly(customer);

    byte[] unknown = customer.clone();
    unknown[0] = "?".getBytes(SourceEncoding.EBCDIC_CP037.charset())[0];
    Map<String, Object> unknownDecoded = codec.decode(unknown, SourceEncoding.EBCDIC_CP037);
    assertThat(unknownDecoded.get("EXPORT-RECORD-DATA"))
        .isEqualTo(Map.of("$type", "UNKNOWN_BRANCH",
            "hex", java.util.HexFormat.of().withUpperCase().formatHex(
                java.util.Arrays.copyOfRange(unknown, 40, unknown.length))));
    assertThat(codec.encode(unknownDecoded, SourceEncoding.EBCDIC_CP037)).containsExactly(unknown);
  }
}
