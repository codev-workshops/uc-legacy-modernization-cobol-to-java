package com.carddemo.batch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test that the Spring context loads successfully.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.files.dalytran=src/test/resources/test-dailytran.txt",
        "app.files.xreffile=src/test/resources/test-cardxref.txt",
        "app.files.acctfile=src/test/resources/test-acctdata.txt"
})
class Cbtrn01cApplicationTest {

    @Test
    void contextLoads() {
        // Verify Spring context loads without errors
    }
}
