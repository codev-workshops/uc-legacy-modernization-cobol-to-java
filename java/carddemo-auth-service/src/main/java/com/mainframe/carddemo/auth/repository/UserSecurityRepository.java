package com.mainframe.carddemo.auth.repository;

import com.mainframe.carddemo.auth.entity.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, String> {

    List<UserSecurity> findByUsrType(String usrType);
}
