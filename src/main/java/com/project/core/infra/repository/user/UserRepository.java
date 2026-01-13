package com.project.core.infra.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.core.infra.entity.customer.Customer;


public interface UserRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByContactEnc(String contactEnc);
}
