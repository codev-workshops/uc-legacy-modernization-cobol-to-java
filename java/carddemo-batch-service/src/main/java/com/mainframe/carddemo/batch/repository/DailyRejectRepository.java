package com.mainframe.carddemo.batch.repository;

import com.mainframe.carddemo.batch.entity.DailyReject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyRejectRepository extends JpaRepository<DailyReject, Integer> {
}
