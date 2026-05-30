package com.carddemo.online.service;

import com.carddemo.common.entity.Account;
import com.carddemo.common.entity.Card;
import com.carddemo.common.entity.CardXref;
import com.carddemo.common.entity.Customer;
import com.carddemo.common.repository.AccountRepository;
import com.carddemo.common.repository.CardRepository;
import com.carddemo.common.repository.CardXrefRepository;
import com.carddemo.common.repository.CustomerRepository;
import com.carddemo.online.dto.AccountResponse;
import com.carddemo.online.dto.AccountUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private Account sampleAccount;
    private CardXref sampleXref;
    private Card sampleCard;
    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleAccount = new Account();
        sampleAccount.setAcctId(1000000001L);
        sampleAccount.setActiveStatus("Y");
        sampleAccount.setCurrBal(new BigDecimal("1500.00"));
        sampleAccount.setCreditLimit(new BigDecimal("5000.00"));
        sampleAccount.setCashCreditLimit(new BigDecimal("1500.00"));
        sampleAccount.setOpenDate("2020-01-15");
        sampleAccount.setExpirationDate("2025-01-15");
        sampleAccount.setAddrZip("60601");

        sampleXref = new CardXref();
        sampleXref.setXrefCardNum("4111111111111111");
        sampleXref.setAcctId(1000000001L);
        sampleXref.setCustId(100001L);

        sampleCard = new Card();
        sampleCard.setCardNum("4111111111111111");
        sampleCard.setAcctId(1000000001L);
        sampleCard.setEmbossedName("JOHN DOE");
        sampleCard.setActiveStatus("Y");
        sampleCard.setExpirationDate("2025-01-15");

        sampleCustomer = new Customer();
        sampleCustomer.setCustId(100001L);
        sampleCustomer.setFirstName("John");
        sampleCustomer.setLastName("Doe");
    }

    @Test
    void getAccount_withCardsAndCustomer() {
        when(accountRepository.findById(1000000001L))
                .thenReturn(Optional.of(sampleAccount));
        when(cardXrefRepository.findByAcctId(1000000001L))
                .thenReturn(List.of(sampleXref));
        when(cardRepository.findAllById(Set.of("4111111111111111")))
                .thenReturn(List.of(sampleCard));
        when(customerRepository.findById(100001L))
                .thenReturn(Optional.of(sampleCustomer));

        AccountResponse resp = accountService.getAccount(1000000001L);

        assertEquals(1000000001L, resp.getAcctId());
        assertEquals("Y", resp.getActiveStatus());
        assertEquals(1, resp.getCards().size());
        assertEquals("4111111111111111", resp.getCards().get(0).getCardNum());
        assertNotNull(resp.getCustomer());
        assertEquals("John", resp.getCustomer().getFirstName());
    }

    @Test
    void getAccount_noXrefs() {
        when(accountRepository.findById(1000000001L))
                .thenReturn(Optional.of(sampleAccount));
        when(cardXrefRepository.findByAcctId(1000000001L))
                .thenReturn(List.of());

        AccountResponse resp = accountService.getAccount(1000000001L);

        assertEquals(1000000001L, resp.getAcctId());
        assertEquals(0, resp.getCards().size());
        assertNull(resp.getCustomer());
    }

    @Test
    void getAccount_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountService.AccountNotFoundException.class,
                () -> accountService.getAccount(999L));
    }

    @Test
    void getAccount_customerNotFound() {
        when(accountRepository.findById(1000000001L))
                .thenReturn(Optional.of(sampleAccount));
        when(cardXrefRepository.findByAcctId(1000000001L))
                .thenReturn(List.of(sampleXref));
        when(cardRepository.findAllById(Set.of("4111111111111111")))
                .thenReturn(List.of(sampleCard));
        when(customerRepository.findById(100001L))
                .thenReturn(Optional.empty());

        AccountResponse resp = accountService.getAccount(1000000001L);

        assertEquals(1, resp.getCards().size());
        assertNull(resp.getCustomer());
    }

    @Test
    void updateAccount_allFields() {
        when(accountRepository.findById(1000000001L))
                .thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any(Account.class)))
                .thenReturn(sampleAccount);

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("N");
        request.setCreditLimit(new BigDecimal("10000.00"));
        request.setCashCreditLimit(new BigDecimal("3000.00"));
        request.setExpirationDate("2030-01-15");
        request.setReissueDate("2025-06-01");
        request.setAddrZip("90210");
        request.setGroupId("GRP001");

        AccountResponse resp = accountService.updateAccount(1000000001L, request);

        assertEquals("N", resp.getActiveStatus());
        assertEquals(new BigDecimal("10000.00"), resp.getCreditLimit());
        assertEquals(new BigDecimal("3000.00"), resp.getCashCreditLimit());
        assertEquals("2030-01-15", resp.getExpirationDate());
        assertEquals("2025-06-01", resp.getReissueDate());
        assertEquals("90210", resp.getAddrZip());
        assertEquals("GRP001", resp.getGroupId());
        verify(accountRepository).save(sampleAccount);
    }

    @Test
    void updateAccount_partialFields() {
        when(accountRepository.findById(1000000001L))
                .thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any(Account.class)))
                .thenReturn(sampleAccount);

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setCreditLimit(new BigDecimal("8000.00"));

        AccountResponse resp = accountService.updateAccount(1000000001L, request);

        assertEquals("Y", resp.getActiveStatus());
        assertEquals(new BigDecimal("8000.00"), resp.getCreditLimit());
    }

    @Test
    void updateAccount_notFound() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("N");

        assertThrows(AccountService.AccountNotFoundException.class,
                () -> accountService.updateAccount(999L, request));
    }

    @Test
    void listAccounts_noFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(List.of(sampleAccount));
        when(accountRepository.findAll(pageable)).thenReturn(page);
        when(cardXrefRepository.findByAcctIdIn(Set.of(1000000001L)))
                .thenReturn(List.of(sampleXref));
        when(customerRepository.findAllById(Set.of(100001L)))
                .thenReturn(List.of(sampleCustomer));

        Page<AccountResponse> result = accountService.listAccounts(null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1000000001L, result.getContent().get(0).getAcctId());
        assertNotNull(result.getContent().get(0).getCustomer());
    }

    @Test
    void listAccounts_filterByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(List.of(sampleAccount));
        when(accountRepository.findByActiveStatus("Y", pageable)).thenReturn(page);
        when(cardXrefRepository.findByAcctIdIn(Set.of(1000000001L)))
                .thenReturn(List.of(sampleXref));
        when(customerRepository.findAllById(Set.of(100001L)))
                .thenReturn(List.of(sampleCustomer));

        Page<AccountResponse> result = accountService.listAccounts("Y", null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listAccounts_filterByCustomerId() {
        Pageable pageable = PageRequest.of(0, 10);
        when(cardXrefRepository.findByCustId(100001L))
                .thenReturn(List.of(sampleXref));
        Page<Account> page = new PageImpl<>(List.of(sampleAccount));
        when(accountRepository.findByAcctIdIn(Set.of(1000000001L), pageable))
                .thenReturn(page);
        when(cardXrefRepository.findByAcctIdIn(Set.of(1000000001L)))
                .thenReturn(List.of(sampleXref));
        when(customerRepository.findAllById(Set.of(100001L)))
                .thenReturn(List.of(sampleCustomer));

        Page<AccountResponse> result = accountService.listAccounts(null, 100001L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listAccounts_filterByCustomerIdAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        when(cardXrefRepository.findByCustId(100001L))
                .thenReturn(List.of(sampleXref));
        Page<Account> page = new PageImpl<>(List.of(sampleAccount));
        when(accountRepository.findByAcctIdInAndActiveStatus(
                Set.of(1000000001L), "Y", pageable))
                .thenReturn(page);
        when(cardXrefRepository.findByAcctIdIn(Set.of(1000000001L)))
                .thenReturn(List.of(sampleXref));
        when(customerRepository.findAllById(Set.of(100001L)))
                .thenReturn(List.of(sampleCustomer));

        Page<AccountResponse> result = accountService.listAccounts("Y", 100001L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listAccounts_customerWithNoAccounts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(cardXrefRepository.findByCustId(999L)).thenReturn(List.of());

        Page<AccountResponse> result = accountService.listAccounts(null, 999L, pageable);

        assertEquals(0, result.getTotalElements());
    }
}
