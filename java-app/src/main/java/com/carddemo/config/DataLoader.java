package com.carddemo.config;

import com.carddemo.model.*;
import com.carddemo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads legacy COBOL fixed-width data files into the H2 database at startup.
 * Parses the ASCII sample data from the original CardDemo mainframe application.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AccountRepository accountRepo;
    private final CardRepository cardRepo;
    private final CustomerRepository customerRepo;
    private final CardCrossReferenceRepository xrefRepo;
    private final TransactionTypeRepository tranTypeRepo;
    private final UserSecurityRepository userRepo;

    public DataLoader(AccountRepository accountRepo, CardRepository cardRepo,
                      CustomerRepository customerRepo, CardCrossReferenceRepository xrefRepo,
                      TransactionTypeRepository tranTypeRepo, UserSecurityRepository userRepo) {
        this.accountRepo = accountRepo;
        this.cardRepo = cardRepo;
        this.customerRepo = customerRepo;
        this.xrefRepo = xrefRepo;
        this.tranTypeRepo = tranTypeRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        loadAccounts();
        loadCards();
        loadCustomers();
        loadCardXref();
        loadTransactionTypes();
        loadUsers();
    }

    private void loadAccounts() {
        List<String> lines = readDataFile("data/acctdata.txt");
        List<Account> accounts = new ArrayList<>();
        for (String line : lines) {
            if (line.length() < 100) continue;
            try {
                Account a = new Account();
                a.setAccountId(Long.parseLong(line.substring(0, 11).trim()));
                a.setActiveStatus(line.substring(11, 12).trim());
                // S9(10)V99 = 12 chars each (sign overpunch on last digit)
                a.setCurrentBalance(parseCobolDecimal(line.substring(12, 24)));
                a.setCreditLimit(parseCobolDecimal(line.substring(24, 36)));
                a.setCashCreditLimit(parseCobolDecimal(line.substring(36, 48)));
                a.setOpenDate(parseDate(line.substring(48, 58)));
                a.setExpirationDate(parseDate(line.substring(58, 68)));
                a.setReissueDate(parseDate(line.substring(68, 78)));
                a.setCurrentCycleCredit(parseCobolDecimal(line.substring(78, 90)));
                a.setCurrentCycleDebit(parseCobolDecimal(line.substring(90, 102)));
                a.setAddressZip(safeSubstring(line, 102, 112));
                a.setGroupId(safeSubstring(line, 112, 122));
                accounts.add(a);
            } catch (Exception e) {
                log.warn("Skipping account line: {}", e.getMessage());
            }
        }
        accountRepo.saveAll(accounts);
        log.info("Loaded {} accounts", accounts.size());
    }

    private void loadCards() {
        List<String> lines = readDataFile("data/carddata.txt");
        List<Card> cards = new ArrayList<>();
        for (String line : lines) {
            if (line.length() < 80) continue;
            try {
                Card c = new Card();
                c.setCardNumber(line.substring(0, 16).trim());
                c.setAccountId(Long.parseLong(line.substring(16, 27).trim()));
                c.setCvvCode(Integer.parseInt(line.substring(27, 30).trim()));
                c.setEmbossedName(line.substring(30, 80).trim());
                c.setExpirationDate(parseDate(line.substring(80, 90)));
                c.setActiveStatus(line.substring(90, 91).trim());
                cards.add(c);
            } catch (Exception e) {
                log.warn("Skipping card line: {}", e.getMessage());
            }
        }
        cardRepo.saveAll(cards);
        log.info("Loaded {} cards", cards.size());
    }

    private void loadCustomers() {
        List<String> lines = readDataFile("data/custdata.txt");
        List<Customer> customers = new ArrayList<>();
        for (String line : lines) {
            if (line.length() < 300) continue;
            try {
                Customer c = new Customer();
                c.setCustomerId(Long.parseLong(line.substring(0, 9).trim()));
                c.setFirstName(line.substring(9, 34).trim());
                c.setMiddleName(line.substring(34, 59).trim());
                c.setLastName(line.substring(59, 84).trim());
                c.setAddressLine1(line.substring(84, 134).trim());
                c.setAddressLine2(line.substring(134, 184).trim());
                c.setAddressLine3(line.substring(184, 234).trim());
                c.setStateCode(line.substring(234, 236).trim());
                c.setCountryCode(line.substring(236, 239).trim());
                c.setZipCode(line.substring(239, 249).trim());
                c.setPhone1(line.substring(249, 264).trim());
                c.setPhone2(line.substring(264, 279).trim());
                c.setSsn(parseLongSafe(line.substring(279, 288).trim()));
                c.setGovtIssuedId(line.substring(288, 308).trim());
                c.setDateOfBirth(parseDate(line.substring(308, 318)));
                c.setEftAccountId(line.substring(318, 328).trim());
                c.setPrimaryCardHolder(line.substring(328, 329).trim());
                c.setFicoCreditScore(parseIntSafe(line.substring(329, 332).trim()));
                customers.add(c);
            } catch (Exception e) {
                log.warn("Skipping customer line: {}", e.getMessage());
            }
        }
        customerRepo.saveAll(customers);
        log.info("Loaded {} customers", customers.size());
    }

    private void loadCardXref() {
        List<String> lines = readDataFile("data/cardxref.txt");
        List<CardCrossReference> xrefs = new ArrayList<>();
        for (String line : lines) {
            if (line.length() < 36) continue;
            try {
                CardCrossReference x = new CardCrossReference();
                x.setCardNumber(line.substring(0, 16).trim());
                x.setCustomerId(Long.parseLong(line.substring(16, 25).trim()));
                x.setAccountId(Long.parseLong(line.substring(25, 36).trim()));
                xrefs.add(x);
            } catch (Exception e) {
                log.warn("Skipping xref line: {}", e.getMessage());
            }
        }
        xrefRepo.saveAll(xrefs);
        log.info("Loaded {} card cross-references", xrefs.size());
    }

    private void loadTransactionTypes() {
        List<String> lines = readDataFile("data/trantype.txt");
        List<TransactionType> types = new ArrayList<>();
        for (String line : lines) {
            if (line.length() < 3) continue;
            try {
                String code = line.substring(0, 2).trim();
                String desc = line.length() > 2 ? line.substring(2, Math.min(52, line.length())).trim() : "";
                if (!code.isEmpty()) {
                    types.add(new TransactionType(code, desc));
                }
            } catch (Exception e) {
                log.warn("Skipping tran type line: {}", e.getMessage());
            }
        }
        tranTypeRepo.saveAll(types);
        log.info("Loaded {} transaction types", types.size());
    }

    private void loadUsers() {
        if (userRepo.count() > 0) return;
        UserSecurity admin = new UserSecurity();
        admin.setUserId("ADMIN001");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setPassword("PASSWORD");
        admin.setUserType("A");
        userRepo.save(admin);

        UserSecurity user = new UserSecurity();
        user.setUserId("USER0001");
        user.setFirstName("Regular");
        user.setLastName("User");
        user.setPassword("PASSWORD");
        user.setUserType("U");
        userRepo.save(user);
        log.info("Loaded default users");
    }

    private List<String> readDataFile(String path) {
        List<String> lines = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource(path);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        lines.add(line);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not read data file {}: {}", path, e.getMessage());
        }
        return lines;
    }

    private BigDecimal parseCobolDecimal(String raw) {
        String s = raw.trim().replace("{", "0").replace("}", "0");
        try {
            long val = Long.parseLong(s);
            return BigDecimal.valueOf(val, 2);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String safeSubstring(String s, int start, int end) {
        if (s.length() < end) return s.substring(start).trim();
        return s.substring(start, end).trim();
    }

    private Long parseLongSafe(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    private Integer parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }
}
