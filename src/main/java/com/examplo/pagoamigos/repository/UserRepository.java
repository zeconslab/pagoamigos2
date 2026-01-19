package com.examplo.pagoamigos.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examplo.pagoamigos.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
