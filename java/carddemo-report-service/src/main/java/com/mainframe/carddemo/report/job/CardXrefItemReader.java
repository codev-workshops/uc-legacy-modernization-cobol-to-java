package com.mainframe.carddemo.report.job;

import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;

import java.util.Iterator;
import java.util.List;

public class CardXrefItemReader implements ItemReader<CardXrefDto>, ItemStream {

    private final AccountServiceClient accountServiceClient;
    private Iterator<CardXrefDto> iterator;

    public CardXrefItemReader(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        iterator = null;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
    }

    @Override
    public void close() throws ItemStreamException {
        iterator = null;
    }

    @Override
    public CardXrefDto read() {
        if (iterator == null) {
            List<CardXrefDto> allXrefs = loadAllXrefs();
            iterator = allXrefs.iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<CardXrefDto> loadAllXrefs() {
        List<CardXrefDto> result = new java.util.ArrayList<>();
        List<com.mainframe.carddemo.common.dto.AccountDto> accounts = accountServiceClient.getAllAccounts();
        if (accounts != null) {
            for (com.mainframe.carddemo.common.dto.AccountDto acct : accounts) {
                List<CardXrefDto> xrefs = accountServiceClient.getXrefByAccountId(acct.getAccountId());
                if (xrefs != null) {
                    result.addAll(xrefs);
                }
            }
        }
        return result;
    }
}
