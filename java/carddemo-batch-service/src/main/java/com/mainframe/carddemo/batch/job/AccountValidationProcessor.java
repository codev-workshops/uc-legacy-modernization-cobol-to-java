package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.AccountDto;
import org.springframework.batch.item.ItemProcessor;

public class AccountValidationProcessor implements ItemProcessor<PostingResult, PostingResult> {

    private final AccountServiceClient accountServiceClient;

    public AccountValidationProcessor(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    @Override
    public PostingResult process(PostingResult item) {
        if (item.isRejected()) {
            return item;
        }
        try {
            AccountDto account = accountServiceClient.getInternalAccountById(item.getAccountId());
            if (account == null) {
                item.reject(101, "Account not found");
                return item;
            }
            item.setCreditLimit(account.getCreditLimit());
            item.setCurrentCycleCredit(account.getCurrentCycleCredit());
            item.setCurrentCycleDebit(account.getCurrentCycleDebit());
            item.setExpirationDate(account.getExpirationDate());
        } catch (Exception e) {
            item.reject(101, "Account not found");
        }
        return item;
    }
}
