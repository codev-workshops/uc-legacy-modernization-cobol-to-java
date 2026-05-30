package com.mainframe.carddemo.auth.repository;

import com.mainframe.carddemo.auth.entity.UserSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserSecurityRepositoryTest {

    @Autowired
    private UserSecurityRepository repository;

    @Test
    void findById_existingUser_returnsUser() {
        Optional<UserSecurity> user = repository.findById("admin01");
        assertThat(user).isPresent();
        assertThat(user.get().getUsrFname()).isEqualTo("System");
        assertThat(user.get().getUsrType()).isEqualTo("A");
    }

    @Test
    void findById_nonExisting_returnsEmpty() {
        Optional<UserSecurity> user = repository.findById("nouser");
        assertThat(user).isEmpty();
    }

    @Test
    void findByUsrType_admin_returnsAdmins() {
        List<UserSecurity> admins = repository.findByUsrType("A");
        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getUsrId()).isEqualTo("admin01");
    }

    @Test
    void findByUsrType_user_returnsUsers() {
        List<UserSecurity> users = repository.findByUsrType("U");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsrId()).isEqualTo("user0001");
    }

    @Test
    void save_newUser_persists() {
        UserSecurity user = new UserSecurity("newuser", "New", "User", "hashedpwd", "U");
        repository.save(user);

        Optional<UserSecurity> found = repository.findById("newuser");
        assertThat(found).isPresent();
        assertThat(found.get().getUsrFname()).isEqualTo("New");
    }

    @Test
    void deleteById_existingUser_removes() {
        repository.deleteById("user0001");
        assertThat(repository.findById("user0001")).isEmpty();
    }
}
