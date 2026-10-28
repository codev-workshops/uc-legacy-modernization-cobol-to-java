package com.carddemo.harness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carddemo.harness.compare.Canonical;
import com.carddemo.harness.compare.ComparisonPolicy;
import com.carddemo.harness.compare.RecordComparator;
import com.carddemo.harness.recon.BalanceRecomputation;
import com.carddemo.harness.recon.RejectReasonParity;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ComparisonAndReconTest {
  @Test
  void policyIgnoresAreReportedAndCanonicalRejectsRounding() throws Exception {
    Path file = Path.of("src/test/resources/comparison-policies.json");
    ComparisonPolicy policy = ComparisonPolicy.load("CBTRN02C-posted-transactions", file);
    var result = new RecordComparator().compare(
        Map.of("TRAN-ID", "A", "TRAN-PROC-TS", "before"),
        Map.of("TRAN-ID", "A", "TRAN-PROC-TS", "after"), policy);
    assertThat(result.equal()).isTrue();
    assertThat(result.skippedFields()).containsExactly("TRAN-PROC-TS");
    assertThat(Canonical.of(new BigDecimal("1.230"), 2)).isEqualTo("1.23");
    assertThatThrownBy(() -> Canonical.of(new BigDecimal("1.234"), 2))
        .isInstanceOf(ArithmeticException.class);
  }

  @Test
  void balanceAndRejectChecksUseSmallSyntheticFileRecords() {
    List<Map<String, Object>> before = List.of(account("100.00", "0.00", "0.00"));
    List<Map<String, Object>> after = List.of(account("105.00", "10.00", "-5.00"));
    List<Map<String, Object>> xrefs =
        List.of(Map.of("XREF-CARD-NUM", "card-1", "XREF-ACCT-ID", "1"));
    List<Map<String, Object>> posted = List.of(
        Map.of("TRAN-CARD-NUM", "card-1", "TRAN-AMT", "10.00"),
        Map.of("TRAN-CARD-NUM", "card-1", "TRAN-AMT", "-5.00"));
    assertThat(new BalanceRecomputation().check(before, xrefs, posted, after)).isEmpty();

    List<Map<String, Object>> rejects =
        List.of(reject("100"), reject("101"), reject("109"));
    var parity = new RejectReasonParity().compare(rejects, rejects, 2, 5);
    assertThat(parity.parity()).isTrue();
    assertThat(parity.postedPlusRejectedEqualsInput()).isTrue();
    assertThat(parity.leftCounts()).containsEntry("100", 1).containsEntry("101", 1)
        .containsEntry("109", 1);
  }

  private Map<String, Object> account(String balance, String credit, String debit) {
    return Map.of("ACCT-ID", "1", "ACCT-CURR-BAL", balance,
        "ACCT-CURR-CYC-CREDIT", credit, "ACCT-CURR-CYC-DEBIT", debit);
  }

  private Map<String, Object> reject(String reason) {
    return Map.of("WS-VALIDATION-FAIL-REASON", reason);
  }
}
