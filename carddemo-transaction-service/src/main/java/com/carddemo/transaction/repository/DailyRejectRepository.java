package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.DailyReject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyRejectRepository extends JpaRepository<DailyReject, Integer> {
}
