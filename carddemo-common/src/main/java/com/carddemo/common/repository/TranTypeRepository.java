package com.carddemo.common.repository;

import com.carddemo.common.entity.TranType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranTypeRepository extends JpaRepository<TranType, String> {
}
