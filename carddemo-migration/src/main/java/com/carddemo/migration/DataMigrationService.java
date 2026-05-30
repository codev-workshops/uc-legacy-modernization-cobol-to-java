package com.carddemo.migration;

import com.carddemo.common.codec.ZonedDecimalCodec;
import com.carddemo.common.entity.*;
import com.carddemo.common.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class DataMigrationService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationService.class);

    private final AccountRepository accountRepo;
    private final CardRepository cardRepo;
    private final CardXrefRepository cardXrefRepo;
    private final CustomerRepository customerRepo;
    private final DailyTransactionRepository dailyTranRepo;
    private final DisclosureGroupRepository discGroupRepo;
    private final TranCatBalanceRepository tcatBalRepo;
    private final TranCategoryRepository tranCatRepo;
    private final TranTypeRepository tranTypeRepo;

    public DataMigrationService(
            AccountRepository accountRepo,
            CardRepository cardRepo,
            CardXrefRepository cardXrefRepo,
            CustomerRepository customerRepo,
            DailyTransactionRepository dailyTranRepo,
            DisclosureGroupRepository discGroupRepo,
            TranCatBalanceRepository tcatBalRepo,
            TranCategoryRepository tranCatRepo,
            TranTypeRepository tranTypeRepo) {
        this.accountRepo = accountRepo;
        this.cardRepo = cardRepo;
        this.cardXrefRepo = cardXrefRepo;
        this.customerRepo = customerRepo;
        this.dailyTranRepo = dailyTranRepo;
        this.discGroupRepo = discGroupRepo;
        this.tcatBalRepo = tcatBalRepo;
        this.tranCatRepo = tranCatRepo;
        this.tranTypeRepo = tranTypeRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            log.info("No data directory specified. Skipping migration.");
            return;
        }
        Path dataDir = Path.of(args[0]);
        if (!Files.isDirectory(dataDir)) {
            log.error("Data directory does not exist: {}", dataDir);
            return;
        }
        migrateAll(dataDir);
    }

    @Transactional
    public void migrateAll(Path dataDir) throws IOException {
        loadAccounts(dataDir.resolve("acctdata.txt"));
        loadCards(dataDir.resolve("carddata.txt"));
        loadCardXrefs(dataDir.resolve("cardxref.txt"));
        loadCustomers(dataDir.resolve("custdata.txt"));
        loadDailyTransactions(dataDir.resolve("dailytran.txt"));
        loadDisclosureGroups(dataDir.resolve("discgrp.txt"));
        loadTranCatBalances(dataDir.resolve("tcatbal.txt"));
        loadTranCategories(dataDir.resolve("trancatg.txt"));
        loadTranTypes(dataDir.resolve("trantype.txt"));
        log.info("Migration complete.");
    }

    private void loadAccounts(Path file) throws IOException {
        if (!Files.exists(file)) { log.warn("File not found: {}", file); return; }
        List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.isBlank()) continue;
            // CVACT01Y: RECLN 300
            Account a = new Account();
            a.setAcctId(Long.parseLong(line.substring(0, 11).trim()));
            a.setActiveStatus(line.substring(11, 12));
            a.setCurrBal(decodeZoned(line, 12, 12, 2));
            a.setCreditLimit(decodeZoned(line, 24, 12, 2));
            a.setCashCreditLimit(decodeZoned(line, 36, 12, 2));
            a.setOpenDate(line.substring(48, 58).trim());
            a.setExpirationDate(line.substring(58, 68).trim());
            a.setReissueDate(line.substring(68, 78).trim());
            a.setCurrCycCredit(decodeZoned(line, 78, 12, 2));
            a.setCurrCycDebit(decodeZoned(line, 90, 12, 2));
            a.setAddrZip(line.substring(102, 112).trim());
            a.setGroupId(line.substring(112, 122).trim());
            accountRepo.save(a);
        }
        log.info("Loaded {} accounts", lines.stream().filter(l -> !l.isBlank()).count());
    }

    private void loadCards(Path file) throws IOException {
        if (!Files.exists(file)) { log.warn("File not found: {}", file); return; }
        List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.isBlank()) continue;
            // CVACT02Y: RECLN 150
            Card c = new Card();
            c.setCardNum(line.substring(0, 16).trim());
            c.setAcctId(Long.parseLong(line.substring(16, 27).trim()));
            c.setCvvCd(Integer.parseInt(line.substring(27, 30).trim()));
            c.setEmbossedName(line.substring(30, 80).trim());
            c.setExpirationDate(line.substring(80, 90).trim());
            c.setActiveStatus(line.substring(90, 91));
            cardRepo.save(c);
        }
        log.info("Loaded {} cards", lines.stream().filter(l -> !l.isBlank()).count());
    }

    private void loadCardXrefs(Path file) throws IOException {
        if (!Files.exists(file)) { log.warn("File not found: {}", file); return; }
        List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.isBlank()) continue;
            // CVACT03Y: RECLN 50
            // But actual data lines are shorter — just the meaningful fields
            CardXref x = new CardXref();
            x.setXrefCardNum(line.substring(0, 16).trim());
            x.setCustId(Long.parseLong(line.substring(16, 25).trim()));
            x.setAcctId(Long.parseLong(line.substring(25, 36).trim()));
            cardXrefRepo.save(x);
        }
        log.info("Loaded {} card xrefs", lines.stream().filter(l -> !l.isBlank()).count());
    }

    private void loadCustomers(Path file) throws IOException {
        if (!Files.exists(file)) { log.warn("File not found: {}", file); return; }
        List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.isBlank()) continue;
            // CVCUS01Y: RECLN 500
            Customer c = new Customer();
            c.setCustId(Long.parseLong(line.substring(0, 9).trim()));
            c.setFirstName(line.substring(9, 34).trim());
            c.setMiddleName(line.substring(34, 59).trim());
            c.setLastName(line.substring(59, 84).trim());
            c.setAddrLine1(line.substring(84, 134).trim());
            c.setAddrLine2(line.substring(134, 184).trim());
            c.setAddrLine3(line.substring(184, 234).trim());
            c.setStateCode(line.substring(234, 236).trim());
            c.setCountryCode(line.substring(236, 239).trim());
            c.setZip(line.substring(239, 249).trim());
            c.setPhone1(line.substring(249, 264).trim());
            c.setPhone2(line.substring(264, 279).trim());
            c.setSsn(Long.parseLong(line.substring(279, 288).trim()));
            c.setGovtIssuedId(line.substring(288, 308).trim());
            c.setDob(line.substring(308, 318).trim());
            c.setEftAccountId(line.substring(318, 328).trim());
            c.setPriCardHolderInd(line.substring(328, 329));
            c.setFicoCreditScore(Integer.parseInt(line.substring(329, 332).trim()));
            customerRepo.save(c);
        }
        log.info("Loaded {} customers", lines.stream().filter(l -> !l.isBlank()).count());
    }

    private void loadDailyTransactions(Path file) throws IOException {
        if (!Files.exists(file)) { log.warn("File not found: {}", file); return; }
        List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.isBlank()) continue;
            // CVTRA06Y: same as CVTRA05Y, RECLN 350
            DailyTransaction dt = parseDailyTransaction(line);
            dailyTranRepo.save(dt);
        }
        log.info("Loaded {} daily transactions", lines.stream().filter(l -> !l.isBlank()).count());
    }

    private DailyTransaction parseDailyTransaction(String line) {
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId(line.substring(0, 16).trim());
        dt.setTypeCd(line.substring(16, 18).trim());
        dt.setCatCd(Integer.parseInt(line.substring(18, 22).trim()));
        dt.setSource(line.substring(22, 32).trim());
        dt.setDesc(line.substring(32, 132).trim());
        dt.setAmt(decodeZoned(line, 132, 11, 2));
        dt.setMerchantId(Long.parseLong(line.substring(143, 152).trim()));
        dt.setMerchantName(line.substring(152, 202).trim());
        dt.setMerchantCity(line.substring(202, 252).trim());
        dt.setMerchantZip(line.substring(252, 262).trim());
        dt.setCardNum(line.substring(262, 278).trim());
        dt.setOrigTs(line.substring(278, 304).trim());
        dt.setProcTs(safeSubstring(line, 304, 330).trim());
        return dt;
    }

    private void loadDisclosureGroups(Path file) throws IOException {
        if (!Files.exists(file)) { log.warn("File not found: {}", file); return; }
        List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.isBlank()) continue;
            // CVTRA02Y: RECLN 50
            DisclosureGroup dg = new DisclosureGroup();
            dg.setAcctGroupId(line.substring(0, 10).trim());
            dg.setTypeCd(line.substring(10, 12).trim());
            dg.setCatCd(Integer.parseInt(line.substring(12, 16).trim()));
            dg.setIntRate(decodeZoned(line, 16, 6, 2));
            discGroupRepo.save(dg);
        }
        log.info("Loaded {} disclosure groups", lines.stream().filter(l -> !l.isBlank()).count());
    }

    private void loadTranCatBalances(Path file) throws IOException {
        if (!Files.exists(file)) { log.warn("File not found: {}", file); return; }
        List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.isBlank()) continue;
            String cleaned = line.replaceAll("\\r", "");
            // CVTRA01Y: RECLN 50
            TranCatBalance tcb = new TranCatBalance();
            tcb.setAcctId(Long.parseLong(cleaned.substring(0, 11).trim()));
            tcb.setTypeCd(cleaned.substring(11, 13).trim());
            tcb.setCatCd(Integer.parseInt(cleaned.substring(13, 17).trim()));
            tcb.setTranCatBal(decodeZoned(cleaned, 17, 11, 2));
            tcatBalRepo.save(tcb);
        }
        log.info("Loaded {} tran cat balances", lines.stream().filter(l -> !l.isBlank()).count());
    }

    private void loadTranCategories(Path file) throws IOException {
        if (!Files.exists(file)) { log.warn("File not found: {}", file); return; }
        List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.isBlank()) continue;
            String cleaned = line.replaceAll("\\r", "");
            // CVTRA04Y: RECLN 60
            TranCategory tc = new TranCategory();
            tc.setTypeCd(cleaned.substring(0, 2).trim());
            tc.setCatCd(Integer.parseInt(cleaned.substring(2, 6).trim()));
            tc.setTranCatTypeDesc(cleaned.substring(6, 56).trim());
            tranCatRepo.save(tc);
        }
        log.info("Loaded {} tran categories", lines.stream().filter(l -> !l.isBlank()).count());
    }

    private void loadTranTypes(Path file) throws IOException {
        if (!Files.exists(file)) { log.warn("File not found: {}", file); return; }
        List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
        for (String line : lines) {
            if (line.isBlank()) continue;
            String cleaned = line.replaceAll("\\r", "");
            // CVTRA03Y: RECLN 60
            TranType tt = new TranType();
            tt.setTranType(cleaned.substring(0, 2).trim());
            tt.setTranTypeDesc(cleaned.substring(2, 52).trim());
            tranTypeRepo.save(tt);
        }
        log.info("Loaded {} tran types", lines.stream().filter(l -> !l.isBlank()).count());
    }

    private BigDecimal decodeZoned(String line, int offset, int length, int scale) {
        byte[] bytes = line.substring(offset, offset + length).getBytes(StandardCharsets.ISO_8859_1);
        return ZonedDecimalCodec.decode(bytes, scale);
    }

    private String safeSubstring(String s, int start, int end) {
        if (start >= s.length()) return "";
        return s.substring(start, Math.min(end, s.length()));
    }
}
