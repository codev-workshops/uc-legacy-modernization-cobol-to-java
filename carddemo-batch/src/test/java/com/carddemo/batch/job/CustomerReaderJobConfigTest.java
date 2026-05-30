package com.carddemo.batch.job;

import com.carddemo.common.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerReaderJobConfigTest {

    @Autowired
    private Job customerReaderJob;

    @Autowired
    private Step customerReaderStep;

    @Autowired
    private JpaPagingItemReader<Customer> customerItemReader;

    @Autowired
    private FlatFileItemWriter<Customer> customerReportWriter;

    @Test
    void jobBeanIsConfigured() {
        assertNotNull(customerReaderJob);
        assertEquals("customerReaderJob", customerReaderJob.getName());
    }

    @Test
    void stepBeanIsConfigured() {
        assertNotNull(customerReaderStep);
        assertEquals("customerReaderStep", customerReaderStep.getName());
    }

    @Test
    void readerBeanIsConfigured() {
        assertNotNull(customerItemReader);
    }

    @Test
    void writerBeanIsConfigured() {
        assertNotNull(customerReportWriter);
    }
}
