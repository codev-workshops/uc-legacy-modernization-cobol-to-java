package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.TranCatBalance;
import com.carddemo.transaction.entity.TranCatBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranCatBalanceRepository extends JpaRepository<TranCatBalance, TranCatBalanceId> {

    List<TranCatBalance> findByTrancatAcctId(Long acctId);
}
