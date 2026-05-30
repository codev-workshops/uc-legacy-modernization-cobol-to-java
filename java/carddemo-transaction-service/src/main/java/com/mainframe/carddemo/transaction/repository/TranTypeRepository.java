package com.mainframe.carddemo.transaction.repository;

import com.mainframe.carddemo.transaction.entity.TranType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranTypeRepository extends JpaRepository<TranType, String> {
}
