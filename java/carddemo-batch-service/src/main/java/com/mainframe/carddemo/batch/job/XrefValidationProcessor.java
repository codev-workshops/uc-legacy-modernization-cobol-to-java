package com.mainframe.carddemo.batch.job;

import com.mainframe.carddemo.batch.entity.DailyTransaction;
import com.mainframe.carddemo.common.client.AccountServiceClient;
import com.mainframe.carddemo.common.dto.CardXrefDto;
import org.springframework.batch.item.ItemProcessor;

public class XrefValidationProcessor implements ItemProcessor<DailyTransaction, PostingResult> {

    private final AccountServiceClient accountServiceClient;

    public XrefValidationProcessor(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    @Override
    public PostingResult process(DailyTransaction item) {
        PostingResult result = new PostingResult(item);
        try {
            CardXrefDto xref = accountServiceClient.getXrefByCardNum(item.getTranCardNum());
            if (xref == null) {
                result.reject(100, "Card not found in cross-reference");
                return result;
            }
            result.setAccountId(xref.getAccountId());
        } catch (Exception e) {
            result.reject(100, "Card not found in cross-reference");
        }
        return result;
    }
}
