package com.project.core.infra.repository.user;

import com.project.core.infra.entity.customer.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Customer, Long> {
  Optional<Customer> findByContactEnc(String contactEnc);
}
