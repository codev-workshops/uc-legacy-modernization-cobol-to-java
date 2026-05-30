package com.carddemo.authorization.repository;

import com.carddemo.authorization.entity.AuthFraud;
import com.carddemo.authorization.entity.AuthFraudId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthFraudRepository extends JpaRepository<AuthFraud, AuthFraudId> {
}
