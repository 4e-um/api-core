package com.project.core.infra.repository.customer;

import com.project.core.infra.entity.customer.Customer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
  List<Customer> findByContactEnc(String contactEnc);
}
