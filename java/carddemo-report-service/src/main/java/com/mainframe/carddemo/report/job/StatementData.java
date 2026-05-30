package com.mainframe.carddemo.report.job;

import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.CustomerDto;
import com.mainframe.carddemo.common.dto.TransactionDto;

import java.util.List;

public class StatementData {

    private final AccountDto account;
    private final CustomerDto customer;
    private final String cardNum;
    private final List<TransactionDto> transactions;

    public StatementData(AccountDto account, CustomerDto customer, String cardNum,
                         List<TransactionDto> transactions) {
        this.account = account;
        this.customer = customer;
        this.cardNum = cardNum;
        this.transactions = transactions;
    }

    public AccountDto getAccount() { return account; }
    public CustomerDto getCustomer() { return customer; }
    public String getCardNum() { return cardNum; }
    public List<TransactionDto> getTransactions() { return transactions; }
}
