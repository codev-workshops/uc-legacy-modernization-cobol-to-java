package com.carddemo.authorization.repository;

import com.carddemo.authorization.entity.Authorization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuthorizationRepository extends JpaRepository<Authorization, Integer> {

    List<Authorization> findByCardNum(String cardNum);

    List<Authorization> findByAcctId(Long acctId);

    Page<Authorization> findByCardNum(String cardNum, Pageable pageable);

    List<Authorization> findByAuthTsBefore(LocalDateTime cutoff);
}
