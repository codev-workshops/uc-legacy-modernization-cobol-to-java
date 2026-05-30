package com.mainframe.carddemo.transaction.repository;

import com.mainframe.carddemo.transaction.entity.TranCatBalance;
import com.mainframe.carddemo.transaction.entity.TranCatBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranCatBalanceRepository extends JpaRepository<TranCatBalance, TranCatBalanceId> {
}
