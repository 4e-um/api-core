package com.project.core.infra.repository.customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.core.infra.entity.customer.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByContactHash(String contactHash);
}
