package com.carddemo.batch.statement;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.entity.Transaction;

import java.util.List;

/**
 * DTO holding all data needed to render one account statement.
 * Populated by {@link StatementProcessor} from CardXref → Customer + Account + Transactions.
 */
public class StatementData {

    private final Customer customer;
    private final Account account;
    private final List<Transaction> transactions;

    public StatementData(Customer customer, Account account, List<Transaction> transactions) {
        this.customer = customer;
        this.account = account;
        this.transactions = transactions;
    }

    public Customer getCustomer() { return customer; }
    public Account getAccount() { return account; }
    public List<Transaction> getTransactions() { return transactions; }
}
