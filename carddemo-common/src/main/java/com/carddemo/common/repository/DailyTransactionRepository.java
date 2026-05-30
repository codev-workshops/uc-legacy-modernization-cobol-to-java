package com.carddemo.common.repository;

import com.carddemo.common.entity.DailyTransaction;
import com.carddemo.common.entity.DailyTransactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, DailyTransactionId> {
}
