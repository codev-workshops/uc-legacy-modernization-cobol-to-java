package com.mainframe.carddemo.batch.job;

import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpirationProcessor implements ItemProcessor<PostingResult, PostingResult> {

    @Override
    public PostingResult process(PostingResult item) {
        if (item.isRejected()) {
            return item;
        }

        LocalDateTime origTs = item.getTransaction().getTranOrigTs();
        LocalDate expirationDate = item.getExpirationDate();

        if (origTs != null && expirationDate != null) {
            LocalDate tranDate = origTs.toLocalDate();
            if (tranDate.isAfter(expirationDate)) {
                item.reject(103, "Transaction date exceeds account expiration date");
            }
        }

        return item;
    }
}
