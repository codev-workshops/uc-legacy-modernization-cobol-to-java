package com.carddemo.harness.recon;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RejectReasonParity {
  private static final List<String> REASONS = List.of("100", "101", "102", "103", "109");

  public Result compare(List<Map<String, Object>> leftRejects, List<Map<String, Object>> rightRejects,
      int postedCount, int inputCount) {
    Map<String, Integer> leftCounts = counts(leftRejects);
    Map<String, Integer> rightCounts = counts(rightRejects);
    boolean totalMatches = postedCount + leftRejects.size() == inputCount
        && postedCount + rightRejects.size() == inputCount;
    return new Result(leftCounts, rightCounts, totalMatches, leftCounts.equals(rightCounts));
  }

  private Map<String, Integer> counts(List<Map<String, Object>> rejects) {
    Map<String, Integer> counts = new LinkedHashMap<>();
    REASONS.forEach(code -> counts.put(code, 0));
    for (Map<String, Object> reject : rejects) {
      String code = String.valueOf(reject.get("WS-VALIDATION-FAIL-REASON"));
      if (counts.containsKey(code)) {
        counts.compute(code, (key, value) -> value + 1);
      }
    }
    return counts;
  }

  public record Result(Map<String, Integer> leftCounts, Map<String, Integer> rightCounts,
                       boolean postedPlusRejectedEqualsInput, boolean parity) {
    public Result {
      leftCounts = Map.copyOf(leftCounts);
      rightCounts = Map.copyOf(rightCounts);
    }
  }
}
