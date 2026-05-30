package com.carddemo.common.repository;

import com.carddemo.common.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Page<Account> findByActiveStatus(String activeStatus, Pageable pageable);
    Page<Account> findByAcctIdIn(Set<Long> acctIds, Pageable pageable);
    Page<Account> findByAcctIdInAndActiveStatus(Set<Long> acctIds, String activeStatus, Pageable pageable);
}
