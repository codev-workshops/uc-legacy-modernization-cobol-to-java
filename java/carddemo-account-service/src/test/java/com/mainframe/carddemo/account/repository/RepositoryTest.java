package com.mainframe.carddemo.account.repository;

import com.mainframe.carddemo.account.entity.Account;
import com.mainframe.carddemo.account.entity.Card;
import com.mainframe.carddemo.account.entity.CardXref;
import com.mainframe.carddemo.account.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @BeforeEach
    void setUp() {
        cardXrefRepository.deleteAll();
        cardRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveAccount() {
        Account account = new Account();
        account.setAcctId(1L);
        account.setAcctActiveStatus("Y");
        account.setAcctCurrBal(new BigDecimal("5000.00"));
        account.setAcctCreditLimit(new BigDecimal("10000.00"));
        account.setAcctOpenDate(LocalDate.of(2020, 1, 15));
        account.setAcctAddrZip("10001");
        accountRepository.save(account);

        Account found = accountRepository.findById(1L).orElseThrow();
        assertThat(found.getAcctActiveStatus()).isEqualTo("Y");
        assertThat(found.getAcctCurrBal()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    void shouldSaveAndRetrieveCustomer() {
        Customer customer = new Customer();
        customer.setCustId(100L);
        customer.setCustFirstName("John");
        customer.setCustLastName("Doe");
        customer.setCustAddrZip("90210");
        customerRepository.save(customer);

        Customer found = customerRepository.findById(100L).orElseThrow();
        assertThat(found.getCustFirstName()).isEqualTo("John");
    }

    @Test
    void shouldFindCardsByAccountId() {
        Account account = new Account();
        account.setAcctId(1L);
        account.setAcctActiveStatus("Y");
        accountRepository.save(account);

        Card card1 = new Card();
        card1.setCardNum("4111111111111111");
        card1.setCardAcctId(1L);
        card1.setCardEmbossedName("JOHN DOE");
        card1.setCardActiveStatus("Y");
        cardRepository.save(card1);

        Card card2 = new Card();
        card2.setCardNum("4111111111112222");
        card2.setCardAcctId(1L);
        card2.setCardEmbossedName("JANE DOE");
        card2.setCardActiveStatus("Y");
        cardRepository.save(card2);

        List<Card> cards = cardRepository.findByCardAcctId(1L);
        assertThat(cards).hasSize(2);
    }

    @Test
    void shouldFindXrefByAccountId() {
        Customer customer = new Customer();
        customer.setCustId(100L);
        customer.setCustFirstName("John");
        customerRepository.save(customer);

        Account account = new Account();
        account.setAcctId(1L);
        account.setAcctActiveStatus("Y");
        accountRepository.save(account);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setXrefCustId(100L);
        xref.setXrefAcctId(1L);
        cardXrefRepository.save(xref);

        List<CardXref> xrefs = cardXrefRepository.findByXrefAcctId(1L);
        assertThat(xrefs).hasSize(1);
        assertThat(xrefs.get(0).getXrefCustId()).isEqualTo(100L);
    }

    @Test
    void xref_shouldEnforceForeignKeyToCustomer() {
        Account account = new Account();
        account.setAcctId(1L);
        account.setAcctActiveStatus("Y");
        accountRepository.save(account);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setXrefCustId(999L);
        xref.setXrefAcctId(1L);

        assertThatThrownBy(() -> {
            cardXrefRepository.saveAndFlush(xref);
        }).isInstanceOf(Exception.class);
    }

    @Test
    void xref_shouldEnforceForeignKeyToAccount() {
        Customer customer = new Customer();
        customer.setCustId(100L);
        customer.setCustFirstName("John");
        customerRepository.save(customer);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("4111111111111111");
        xref.setXrefCustId(100L);
        xref.setXrefAcctId(999L);

        assertThatThrownBy(() -> {
            cardXrefRepository.saveAndFlush(xref);
        }).isInstanceOf(Exception.class);
    }
}
