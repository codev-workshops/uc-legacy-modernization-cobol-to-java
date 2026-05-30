package com.carddemo.batch.job.xrefprinter;

import com.carddemo.batch.BatchApplication;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = BatchApplication.class)
class XrefPrinterJobConfigTest {

    @Autowired
    private Job xrefPrinterJob;

    @Autowired
    private Step xrefPrinterStep;

    @Autowired
    private JpaPagingItemReader<?> xrefReader;

    @Test
    void jobBeanIsCreated() {
        assertNotNull(xrefPrinterJob);
        assertEquals(XrefPrinterJobConfig.JOB_NAME, xrefPrinterJob.getName());
    }

    @Test
    void stepBeanIsCreated() {
        assertNotNull(xrefPrinterStep);
        assertEquals(XrefPrinterJobConfig.STEP_NAME, xrefPrinterStep.getName());
    }

    @Test
    void readerBeanIsCreated() {
        assertNotNull(xrefReader);
    }
}
