package com.carddemo.harness.codec;

import java.util.List;
import java.util.Optional;

public record RecordLayout(
    String name,
    String copybookPath,
    int recordLength,
    List<FieldDef> fields,
    Optional<Discriminator> discriminator) {
  public RecordLayout {
    fields = List.copyOf(fields);
    discriminator = discriminator == null ? Optional.empty() : discriminator;
  }

  public FieldDef field(String name) {
    return fields.stream().filter(field -> field.name().equals(name)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown field " + name + " in " + this.name));
  }
}
