package com.mainframe.carddemo.report.job;

import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.client.TransactionServiceClient;
import com.mainframe.carddemo.common.dto.AccountDto;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import com.mainframe.carddemo.common.dto.CustomerDto;
import com.mainframe.carddemo.common.dto.TransactionDto;
import org.springframework.batch.item.ItemProcessor;

import java.util.Collections;
import java.util.List;

public class StatementGenerationProcessor implements ItemProcessor<CardXrefDto, StatementData> {

    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;

    public StatementGenerationProcessor(AccountServiceClient accountServiceClient,
                                        TransactionServiceClient transactionServiceClient) {
        this.accountServiceClient = accountServiceClient;
        this.transactionServiceClient = transactionServiceClient;
    }

    @Override
    public StatementData process(CardXrefDto xref) {
        AccountDto account = accountServiceClient.getInternalAccountById(xref.getAccountId());
        if (account == null) {
            return null;
        }

        CustomerDto customer = null;
        if (xref.getCustomerId() != null) {
            customer = accountServiceClient.getCustomerById(xref.getCustomerId());
        }

        List<TransactionDto> transactions;
        try {
            transactions = transactionServiceClient.getTransactionsByCardNum(xref.getCardNum());
        } catch (Exception e) {
            transactions = Collections.emptyList();
        }

        return new StatementData(account, customer, xref.getCardNum(), transactions);
    }
}
