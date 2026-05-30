package com.mainframe.carddemo.batch.repository;

import com.mainframe.carddemo.batch.entity.TranCatBalance;
import com.mainframe.carddemo.batch.entity.TranCatBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranCatBalanceRepository extends JpaRepository<TranCatBalance, TranCatBalanceId> {
}
