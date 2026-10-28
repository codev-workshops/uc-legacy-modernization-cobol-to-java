package com.carddemo.harness.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class RecordComparator {
  public ComparisonResult compare(
      Map<String, Object> expected, Map<String, Object> actual, ComparisonPolicy policy) {
    List<FieldDiff> diffs = new ArrayList<>();
    List<String> skipped = new ArrayList<>();
    compareValue("", expected, actual, policy, diffs, skipped);
    return new ComparisonResult(diffs, skipped, List.of(), List.of());
  }

  public ComparisonResult compareKeyed(List<Map<String, Object>> expected,
      List<Map<String, Object>> actual, List<String> keys, ComparisonPolicy policy) {
    Map<String, Map<String, Object>> expectedByKey = index(expected, keys);
    Map<String, Map<String, Object>> actualByKey = index(actual, keys);
    List<String> missing = new ArrayList<>();
    List<String> extra = new ArrayList<>();
    List<FieldDiff> diffs = new ArrayList<>();
    List<String> skipped = new ArrayList<>();
    for (String key : expectedByKey.keySet()) {
      if (!actualByKey.containsKey(key)) {
        missing.add(key);
      } else {
        compareValue(key, expectedByKey.get(key), actualByKey.get(key), policy, diffs, skipped);
      }
    }
    for (String key : actualByKey.keySet()) {
      if (!expectedByKey.containsKey(key)) {
        extra.add(key);
      }
    }
    return new ComparisonResult(diffs, skipped, missing, extra);
  }

  private void compareValue(String path, Object expected, Object actual, ComparisonPolicy policy,
      List<FieldDiff> diffs, List<String> skipped) {
    if (!path.isEmpty() && policy.ignores(path)) {
      skipped.add(path);
      return;
    }
    if (expected instanceof Map<?, ?> left && actual instanceof Map<?, ?> right) {
      Set<String> names = new LinkedHashSet<>();
      left.keySet().forEach(key -> names.add(String.valueOf(key)));
      right.keySet().forEach(key -> names.add(String.valueOf(key)));
      for (String name : names) {
        String child = path.isEmpty() ? name : path + "." + name;
        if (!left.containsKey(name) || !right.containsKey(name)) {
          diffs.add(new FieldDiff(child, left.get(name), right.get(name)));
        } else {
          compareValue(child, left.get(name), right.get(name), policy, diffs, skipped);
        }
      }
    } else if (expected instanceof List<?> left && actual instanceof List<?> right) {
      int length = Math.max(left.size(), right.size());
      for (int index = 0; index < length; index++) {
        String child = path + "[" + index + "]";
        if (index >= left.size() || index >= right.size()) {
          diffs.add(new FieldDiff(child, index < left.size() ? left.get(index) : null,
              index < right.size() ? right.get(index) : null));
        } else {
          compareValue(child, left.get(index), right.get(index), policy, diffs, skipped);
        }
      }
    } else if (!java.util.Objects.equals(expected, actual)) {
      diffs.add(new FieldDiff(path, expected, actual));
    }
  }

  private Map<String, Map<String, Object>> index(List<Map<String, Object>> records, List<String> keys) {
    Map<String, Map<String, Object>> index = new LinkedHashMap<>();
    for (Map<String, Object> record : records) {
      String key = keys.stream().map(field -> String.valueOf(record.get(field)))
          .collect(java.util.stream.Collectors.joining("|"));
      if (index.putIfAbsent(key, record) != null) {
        throw new IllegalArgumentException("Duplicate comparison key " + key);
      }
    }
    return index;
  }
}
