package com.project.core.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.core.infra.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByContactEnc(String contactEnc);
}
