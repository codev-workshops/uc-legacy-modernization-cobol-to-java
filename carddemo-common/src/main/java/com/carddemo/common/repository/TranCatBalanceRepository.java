package com.carddemo.common.repository;

import com.carddemo.common.entity.TranCatBalance;
import com.carddemo.common.entity.TranCatBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranCatBalanceRepository extends JpaRepository<TranCatBalance, TranCatBalanceId> {
}
