package com.legal.platform.repository;

import com.legal.platform.model.User;
import com.legal.platform.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(String code);
    Optional<User> findByResetPasswordCode(String code);
    List<User> findByRole(Role role);
    long countByRole(Role role);
}
