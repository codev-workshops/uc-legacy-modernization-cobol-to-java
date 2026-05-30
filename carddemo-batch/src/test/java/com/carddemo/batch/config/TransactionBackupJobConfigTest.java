package com.carddemo.batch.config;

import com.carddemo.batch.BatchApplication;
import com.carddemo.batch.writer.TransactionBackupFileWriter;
import com.carddemo.common.entity.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BatchApplication.class)
@ActiveProfiles("dev")
class TransactionBackupJobConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void jobBeanExists() {
        Job job = context.getBean("transactionBackupJob", Job.class);
        assertNotNull(job);
        assertEquals("transactionBackupJob", job.getName());
    }

    @Test
    void stepBeanExists() {
        Step step = context.getBean("transactionBackupStep", Step.class);
        assertNotNull(step);
        assertEquals("transactionBackupStep", step.getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void readerBeanExists() {
        JpaPagingItemReader<Transaction> reader =
                context.getBean("transactionReader", JpaPagingItemReader.class);
        assertNotNull(reader);
    }

    @Test
    void writerBeanExists() {
        TransactionBackupFileWriter writer =
                context.getBean(TransactionBackupFileWriter.class);
        assertNotNull(writer);
    }
}
