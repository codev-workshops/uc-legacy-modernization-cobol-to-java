package com.carddemo.batch.posting;


import com.carddemo.common.entity.DailyTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PostingTestApplication.class)
class TransactionPostingJobConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void jobBeanExists() {
        Job job = context.getBean("transactionPostingJob", Job.class);
        assertNotNull(job);
        assertEquals("transactionPostingJob", job.getName());
    }

    @Test
    void stepBeanExists() {
        Step step = context.getBean("transactionPostingStep", Step.class);
        assertNotNull(step);
        assertEquals("transactionPostingStep", step.getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void readerBeanExists() {
        JpaPagingItemReader<DailyTransaction> reader =
                context.getBean("dailyTransactionReader", JpaPagingItemReader.class);
        assertNotNull(reader);
    }

    @Test
    void processorBeanExists() {
        TransactionPostingProcessor processor =
                context.getBean(TransactionPostingProcessor.class);
        assertNotNull(processor);
    }

    @Test
    void writerBeanExists() {
        TransactionPostingWriter writer =
                context.getBean(TransactionPostingWriter.class);
        assertNotNull(writer);
    }
}
