package com.mainframe.carddemo.batch.repository;

import com.mainframe.carddemo.batch.entity.BatchTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchTransactionRepository extends JpaRepository<BatchTransaction, String> {
}
