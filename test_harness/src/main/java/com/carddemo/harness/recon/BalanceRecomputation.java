package com.carddemo.harness.recon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BalanceRecomputation {
  public List<BalanceDifference> check(List<Map<String, Object>> beforeAccounts,
      List<Map<String, Object>> cardXrefs, List<Map<String, Object>> postedTransactions,
      List<Map<String, Object>> afterAccounts) {
    Map<String, String> accountByCard = new HashMap<>();
    cardXrefs.forEach(record -> accountByCard.put(string(record, "XREF-CARD-NUM"),
        string(record, "XREF-ACCT-ID")));
    Map<String, BigDecimal> postedByAccount = new HashMap<>();
    Map<String, BigDecimal> creditByAccount = new HashMap<>();
    Map<String, BigDecimal> debitByAccount = new HashMap<>();
    for (Map<String, Object> transaction : postedTransactions) {
      String account = accountByCard.get(string(transaction, "TRAN-CARD-NUM"));
      if (account == null) {
        continue;
      }
      BigDecimal amount = new BigDecimal(string(transaction, "TRAN-AMT"));
      postedByAccount.merge(account, amount, BigDecimal::add);
      (amount.signum() >= 0 ? creditByAccount : debitByAccount)
          .merge(account, amount, BigDecimal::add);
    }
    Map<String, Map<String, Object>> afterByAccount = new HashMap<>();
    afterAccounts.forEach(account -> afterByAccount.put(string(account, "ACCT-ID"), account));
    List<BalanceDifference> differences = new ArrayList<>();
    for (Map<String, Object> before : beforeAccounts) {
      String id = string(before, "ACCT-ID");
      Map<String, Object> after = afterByAccount.get(id);
      if (after == null) {
        differences.add(new BalanceDifference(id, "ACCT-CURR-BAL", string(before, "ACCT-CURR-BAL"),
            null));
        continue;
      }
      compare(differences, id, "ACCT-CURR-BAL",
          new BigDecimal(string(before, "ACCT-CURR-BAL"))
              .add(postedByAccount.getOrDefault(id, BigDecimal.ZERO)),
          new BigDecimal(string(after, "ACCT-CURR-BAL")));
      compare(differences, id, "ACCT-CURR-CYC-CREDIT",
          new BigDecimal(string(before, "ACCT-CURR-CYC-CREDIT"))
              .add(creditByAccount.getOrDefault(id, BigDecimal.ZERO)),
          new BigDecimal(string(after, "ACCT-CURR-CYC-CREDIT")));
      compare(differences, id, "ACCT-CURR-CYC-DEBIT",
          new BigDecimal(string(before, "ACCT-CURR-CYC-DEBIT"))
              .add(debitByAccount.getOrDefault(id, BigDecimal.ZERO)),
          new BigDecimal(string(after, "ACCT-CURR-CYC-DEBIT")));
    }
    return List.copyOf(differences);
  }

  private void compare(List<BalanceDifference> result, String account, String field,
      BigDecimal expected, BigDecimal actual) {
    if (expected.compareTo(actual) != 0) {
      result.add(new BalanceDifference(account, field, expected.toPlainString(), actual.toPlainString()));
    }
  }

  private String string(Map<String, Object> record, String field) {
    return String.valueOf(record.get(field));
  }

  public record BalanceDifference(String accountId, String field, String expected, String actual) {}
}
