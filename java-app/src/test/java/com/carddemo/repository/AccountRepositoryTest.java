package com.carddemo.repository;

import com.carddemo.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.defer-datasource-initialization=false",
    "spring.sql.init.mode=never"
})
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void saveAndFindById() {
        Account a = new Account();
        a.setAccountId(100L);
        a.setActiveStatus("Y");
        a.setCurrentBalance(new BigDecimal("5000.00"));
        a.setCreditLimit(new BigDecimal("10000.00"));
        a.setCashCreditLimit(new BigDecimal("3000.00"));
        a.setOpenDate(LocalDate.of(2020, 1, 1));

        accountRepository.save(a);

        Optional<Account> found = accountRepository.findById(100L);
        assertTrue(found.isPresent());
        assertEquals(new BigDecimal("5000.00"), found.get().getCurrentBalance());
    }

    @Test
    void findByActiveStatus() {
        Account active = new Account();
        active.setAccountId(200L);
        active.setActiveStatus("Y");
        active.setCurrentBalance(BigDecimal.ZERO);

        Account inactive = new Account();
        inactive.setAccountId(201L);
        inactive.setActiveStatus("N");
        inactive.setCurrentBalance(BigDecimal.ZERO);

        accountRepository.saveAll(List.of(active, inactive));

        List<Account> result = accountRepository.findByActiveStatus("Y");
        assertEquals(1, result.size());
        assertEquals(200L, result.get(0).getAccountId());
    }
}
