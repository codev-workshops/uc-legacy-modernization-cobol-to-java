package com.carddemo.harness.compare;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ComparisonPolicy(String caseId, Set<String> ignoredPaths) {
  public ComparisonPolicy {
    ignoredPaths = Set.copyOf(ignoredPaths);
  }

  public static ComparisonPolicy strict(String caseId) {
    return new ComparisonPolicy(caseId, Set.of());
  }

  public static ComparisonPolicy load(String caseId, Path file) throws IOException {
    Map<String, List<String>> configured = new ObjectMapper().readValue(
        Files.readString(file), new TypeReference<>() {});
    return new ComparisonPolicy(caseId, Set.copyOf(configured.getOrDefault(caseId, List.of())));
  }

  public boolean ignores(String path) {
    if (ignoredPaths.contains(path)) {
      return true;
    }
    String fieldName = path.substring(path.lastIndexOf('.') + 1);
    int index = fieldName.indexOf('[');
    if (index >= 0) {
      fieldName = fieldName.substring(0, index);
    }
    return ignoredPaths.contains(fieldName);
  }
}
