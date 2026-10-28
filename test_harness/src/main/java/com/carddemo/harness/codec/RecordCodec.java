package com.carddemo.harness.codec;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RecordCodec {
  private final RecordLayout layout;
  private final AlphanumericCodec alphanumeric = new AlphanumericCodec();
  private final ZonedDecimalCodec zoned = new ZonedDecimalCodec();
  private final PackedDecimalCodec packed = new PackedDecimalCodec();
  private final BinaryCodec binary = new BinaryCodec();

  public RecordCodec(RecordLayout layout) {
    this.layout = layout;
  }

  public LinkedHashMap<String, Object> decode(byte[] record, SourceEncoding encoding) {
    if (record.length != layout.recordLength()) {
      throw new IllegalArgumentException(
          "Expected " + layout.recordLength() + " bytes for " + layout.name() + ", got " + record.length);
    }
    LinkedHashMap<String, Object> decoded = new LinkedHashMap<>();
    for (FieldDef field : layout.fields()) {
      if (field.redefines() == null
          && !(layout.discriminator().isPresent() && field.name().equals("EXPORT-RECORD-DATA"))) {
        if (field.isGroup()) {
          decodeGroup(field, record, encoding, decoded);
        } else {
          decoded.put(field.name(), decodeElementary(field, slice(record, field.offset(), field.length()),
              encoding));
        }
      }
    }
    layout.discriminator().ifPresent(discriminator -> {
      Object rawCode = decoded.get(discriminator.field());
      String branchName = discriminator.branches().get(rawCode);
      FieldDef branch = layout.fields().stream()
          .filter(field -> field.name().equals(branchName)).findFirst().orElse(null);
      if (branch == null) {
        FieldDef payload = layout.field("EXPORT-RECORD-DATA");
        decoded.put(payload.name(), jsonValue(Undecodable.of("UNKNOWN_BRANCH",
            slice(record, payload.offset(), payload.length()))));
      } else {
        decoded.put(branch.name(), decodeObject(branch, record, encoding));
      }
    });
    return decoded;
  }

  public byte[] encode(Map<String, Object> record, SourceEncoding encoding) {
    byte[] output = new byte[layout.recordLength()];
    Arrays.fill(output, (byte) ' ');
    if (encoding == SourceEncoding.EBCDIC_CP037) {
      Arrays.fill(output, (byte) 0x40);
    }
    for (FieldDef field : layout.fields()) {
      if (field.redefines() != null
          || (layout.discriminator().isPresent() && field.name().equals("EXPORT-RECORD-DATA"))) {
        continue;
      }
      Object value = record.get(field.name());
      if (field.isGroup()) {
        encodeGroup(field, output, encoding, record);
      } else {
        encodeElementary(field, output, encoding, value);
      }
    }
    if (layout.discriminator().isPresent()) {
      Discriminator discriminator = layout.discriminator().orElseThrow();
      String code = String.valueOf(record.get(discriminator.field()));
      String selected = discriminator.branches().get(code);
      Object branchValue = record.get(selected);
      if (selected == null) {
        Object undecodable = record.get("EXPORT-RECORD-DATA");
        if (undecodable instanceof Map<?, ?> map) {
          FieldDef payload = layout.field("EXPORT-RECORD-DATA");
          writeBytes(output, payload.offset(), Undecodable.fromMap(castMap(map)).bytes());
        } else {
          throw new IllegalArgumentException("Unknown discriminator code " + code);
        }
      } else {
        FieldDef branch = layout.fields().stream().filter(field -> field.name().equals(selected))
            .findFirst().orElseThrow();
        if (!(branchValue instanceof Map<?, ?> values)) {
          throw new IllegalArgumentException("Missing branch object " + selected);
        }
        encodeGroup(branch, output, encoding, castMap(values));
      }
    }
    return output;
  }

  private void decodeGroup(FieldDef group, byte[] record, SourceEncoding encoding,
      LinkedHashMap<String, Object> target) {
    if (group.occurs() > 1) {
      List<Object> values = new ArrayList<>();
      for (int index = 0; index < group.occurs(); index++) {
        int shift = index * group.length();
        values.add(group.isGroup()
            ? decodeObject(shift(group, shift), record, encoding)
            : decodeElementary(group, slice(record, group.offset() + shift, group.length()), encoding));
      }
      target.put(group.name(), values);
    } else if (group.isGroup()) {
      target.putAll(decodeObject(group, record, encoding));
    } else {
      target.put(group.name(), decodeElementary(group,
          slice(record, group.offset(), group.length()), encoding));
    }
  }

  private LinkedHashMap<String, Object> decodeObject(FieldDef group, byte[] record,
      SourceEncoding encoding) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<>();
    for (FieldDef child : group.children()) {
      if (child.redefines() != null) {
        continue;
      }
      decodeGroup(child, record, encoding, result);
    }
    return result;
  }

  private Object decodeElementary(FieldDef field, byte[] bytes, SourceEncoding encoding) {
    if (field.pic().alphanumeric()) {
      return jsonValue(alphanumeric.decode(bytes, field.pic(), encoding));
    }
    return jsonValue(switch (field.usage()) {
      case COMP_3 -> packed.decode(bytes, field.pic(), encoding);
      case COMP -> binary.decode(bytes, field.pic(), false);
      case COMP_5 -> binary.decode(bytes, field.pic(), true);
      case DISPLAY -> zoned.decode(bytes, field.pic(), encoding);
    });
  }

  private void encodeGroup(FieldDef group, byte[] output, SourceEncoding encoding,
      Map<String, Object> values) {
    Object fieldValue = values.get(group.name());
    if (group.occurs() > 1) {
      if (!(fieldValue instanceof List<?> list) || list.size() != group.occurs()) {
        throw new IllegalArgumentException("Expected " + group.occurs() + " occurrences for " + group.name());
      }
      for (int index = 0; index < group.occurs(); index++) {
        Map<String, Object> occurrence = group.isGroup()
            ? castMap((Map<?, ?>) list.get(index))
            : Map.of(group.name(), list.get(index));
        encodeGroup(shift(group, index * group.length()), output, encoding, occurrence);
      }
    } else if (group.isGroup()) {
      encodeGroupChildren(group, output, encoding, values);
    } else {
      encodeElementary(group, output, encoding, fieldValue);
    }
  }

  private void encodeGroupChildren(FieldDef group, byte[] output, SourceEncoding encoding,
      Map<String, Object> values) {
    for (FieldDef child : group.children()) {
      if (child.redefines() == null) {
        encodeGroup(child, output, encoding, values);
      }
    }
  }

  private void encodeElementary(FieldDef field, byte[] output, SourceEncoding encoding, Object value) {
    if (value == null) {
      throw new IllegalArgumentException("Missing field " + field.name());
    }
    byte[] bytes;
    if (value instanceof Map<?, ?> map && map.containsKey("$type")) {
      bytes = Undecodable.fromMap(castMap(map)).bytes();
    } else if (field.pic().alphanumeric()) {
      bytes = alphanumeric.encode(String.valueOf(value), field.pic(), encoding);
    } else {
      String canonical = String.valueOf(value);
      bytes = switch (field.usage()) {
        case COMP_3 -> packed.encode(canonical, field.pic(), encoding);
        case COMP, COMP_5 -> binary.encode(canonical, field.pic(), encoding);
        case DISPLAY -> zoned.encode(canonical, field.pic(), encoding);
      };
    }
    writeBytes(output, field.offset(), bytes);
  }

  private static FieldDef shift(FieldDef field, int amount) {
    List<FieldDef> shifted = field.children().stream().map(child -> shift(child, amount)).toList();
    return new FieldDef(field.name(), field.offset() + amount, field.length(), field.pic(), field.usage(),
        1, shifted, field.redefines(), field.filler());
  }

  private static byte[] slice(byte[] bytes, int offset, int length) {
    return Arrays.copyOfRange(bytes, offset, offset + length);
  }

  private static void writeBytes(byte[] output, int offset, byte[] bytes) {
    if (offset + bytes.length > output.length) {
      throw new IllegalArgumentException("Field exceeds record length");
    }
    System.arraycopy(bytes, 0, output, offset, bytes.length);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> castMap(Map<?, ?> map) {
    return (Map<String, Object>) map;
  }

  private static Object jsonValue(Object value) {
    if (!(value instanceof Undecodable undecodable)) {
      return value;
    }
    LinkedHashMap<String, Object> object = new LinkedHashMap<>();
    object.put("$type", undecodable.type());
    object.put("hex", undecodable.hex());
    return object;
  }
}
