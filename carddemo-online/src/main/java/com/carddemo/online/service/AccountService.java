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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CardXrefRepository cardXrefRepository,
                          CardRepository cardRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.cardRepository = cardRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long acctId) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + acctId));

        List<CardXref> xrefs = cardXrefRepository.findByAcctId(acctId);

        List<AccountResponse.CardSummary> cards = List.of();
        AccountResponse.CustomerSummary customerSummary = null;

        if (!xrefs.isEmpty()) {
            Set<String> cardNums = xrefs.stream()
                    .map(CardXref::getXrefCardNum)
                    .collect(Collectors.toSet());
            List<Card> cardEntities = cardRepository.findAllById(cardNums);
            cards = cardEntities.stream()
                    .map(c -> new AccountResponse.CardSummary(
                            c.getCardNum(), c.getEmbossedName(),
                            c.getActiveStatus(), c.getExpirationDate()))
                    .toList();

            Long custId = xrefs.get(0).getCustId();
            customerSummary = customerRepository.findById(custId)
                    .map(cust -> new AccountResponse.CustomerSummary(
                            cust.getCustId(), cust.getFirstName(), cust.getLastName()))
                    .orElse(null);
        }

        return toResponse(account, cards, customerSummary);
    }

    @Transactional
    public AccountResponse updateAccount(Long acctId, AccountUpdateRequest request) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + acctId));

        if (request.getActiveStatus() != null) {
            account.setActiveStatus(request.getActiveStatus());
        }
        if (request.getCreditLimit() != null) {
            account.setCreditLimit(request.getCreditLimit());
        }
        if (request.getCashCreditLimit() != null) {
            account.setCashCreditLimit(request.getCashCreditLimit());
        }
        if (request.getExpirationDate() != null) {
            account.setExpirationDate(request.getExpirationDate());
        }
        if (request.getReissueDate() != null) {
            account.setReissueDate(request.getReissueDate());
        }
        if (request.getAddrZip() != null) {
            account.setAddrZip(request.getAddrZip());
        }
        if (request.getGroupId() != null) {
            account.setGroupId(request.getGroupId());
        }

        accountRepository.save(account);
        return toResponse(account, List.of(), null);
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> listAccounts(String activeStatus, Long customerId,
                                               Pageable pageable) {
        Page<Account> page;

        if (customerId != null) {
            List<CardXref> xrefs = cardXrefRepository.findByCustId(customerId);
            Set<Long> acctIds = xrefs.stream()
                    .map(CardXref::getAcctId)
                    .collect(Collectors.toSet());

            if (acctIds.isEmpty()) {
                return Page.empty(pageable);
            }

            if (activeStatus != null) {
                page = accountRepository.findByAcctIdInAndActiveStatus(
                        acctIds, activeStatus, pageable);
            } else {
                page = accountRepository.findByAcctIdIn(acctIds, pageable);
            }
        } else if (activeStatus != null) {
            page = accountRepository.findByActiveStatus(activeStatus, pageable);
        } else {
            page = accountRepository.findAll(pageable);
        }

        // Pre-load xrefs for all accounts on the page
        Set<Long> pageAcctIds = page.getContent().stream()
                .map(Account::getAcctId)
                .collect(Collectors.toSet());
        List<CardXref> allXrefs = pageAcctIds.isEmpty()
                ? List.of()
                : cardXrefRepository.findByAcctIdIn(pageAcctIds);

        Map<Long, Long> acctToCust = allXrefs.stream()
                .collect(Collectors.toMap(
                        CardXref::getAcctId,
                        CardXref::getCustId,
                        (a, b) -> a));

        Set<Long> custIds = allXrefs.stream()
                .map(CardXref::getCustId)
                .collect(Collectors.toSet());
        Map<Long, Customer> custMap = custIds.isEmpty()
                ? Map.of()
                : customerRepository.findAllById(custIds).stream()
                    .collect(Collectors.toMap(Customer::getCustId, c -> c));

        return page.map(account -> {
            Long cId = acctToCust.get(account.getAcctId());
            Customer cust = cId != null ? custMap.get(cId) : null;
            AccountResponse.CustomerSummary cs = cust != null
                    ? new AccountResponse.CustomerSummary(
                            cust.getCustId(), cust.getFirstName(), cust.getLastName())
                    : null;
            return toResponse(account, List.of(), cs);
        });
    }

    private AccountResponse toResponse(Account account,
                                        List<AccountResponse.CardSummary> cards,
                                        AccountResponse.CustomerSummary customer) {
        AccountResponse resp = new AccountResponse();
        resp.setAcctId(account.getAcctId());
        resp.setActiveStatus(account.getActiveStatus());
        resp.setCurrBal(account.getCurrBal());
        resp.setCreditLimit(account.getCreditLimit());
        resp.setCashCreditLimit(account.getCashCreditLimit());
        resp.setOpenDate(account.getOpenDate());
        resp.setExpirationDate(account.getExpirationDate());
        resp.setReissueDate(account.getReissueDate());
        resp.setCurrCycCredit(account.getCurrCycCredit());
        resp.setCurrCycDebit(account.getCurrCycDebit());
        resp.setAddrZip(account.getAddrZip());
        resp.setGroupId(account.getGroupId());
        resp.setCards(cards);
        resp.setCustomer(customer);
        return resp;
    }

    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(String message) {
            super(message);
        }
    }
}
