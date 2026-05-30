package com.mainframe.carddemo.transaction.repository;

import com.mainframe.carddemo.transaction.entity.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {
}
