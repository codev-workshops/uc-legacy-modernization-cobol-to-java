package com.carddemo.migration;

import com.carddemo.common.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MigrationApplication.class)
@ActiveProfiles("dev")
class DataMigrationServiceIT {

    @Autowired
    private DataMigrationService migrationService;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private CardRepository cardRepo;

    @Autowired
    private CardXrefRepository cardXrefRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private DailyTransactionRepository dailyTranRepo;

    @Autowired
    private DisclosureGroupRepository discGroupRepo;

    @Autowired
    private TranCatBalanceRepository tcatBalRepo;

    @Autowired
    private TranCategoryRepository tranCatRepo;

    @Autowired
    private TranTypeRepository tranTypeRepo;

    @Test
    void migrateAsciiDataIntoH2() throws Exception {
        Path dataDir = Paths.get("../app/data/ASCII");
        if (!dataDir.toFile().isDirectory()) {
            // Try from repo root
            dataDir = Paths.get("app/data/ASCII");
        }
        assertTrue(dataDir.toFile().isDirectory(),
                "ASCII data directory must exist at " + dataDir.toAbsolutePath());

        migrationService.migrateAll(dataDir);

        assertEquals(50, accountRepo.count(), "accounts");
        assertEquals(50, cardRepo.count(), "cards");
        assertEquals(50, cardXrefRepo.count(), "card xrefs");
        assertEquals(50, customerRepo.count(), "customers");
        assertEquals(300, dailyTranRepo.count(), "daily transactions");
        assertEquals(51, discGroupRepo.count(), "disclosure groups");
        assertEquals(50, tcatBalRepo.count(), "tran cat balances");
        assertEquals(18, tranCatRepo.count(), "tran categories");
        assertEquals(7, tranTypeRepo.count(), "tran types");
    }
}
