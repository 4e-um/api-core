package com.project.core.infra.repository.subscription;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByCustomer_CustomerId(Long customerId);

    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.planHistory ph LEFT JOIN FETCH ph.plan WHERE s.customer.customerId = :customerId")
    List<Subscription> findAllByCustomer_CustomerIdWithPlan(@Param("customerId") Long customerId);

    long countByCustomerAndStatus(Customer customer, SubscriptionStatus status);

    boolean existsByPhoneNumberAndStatus(String phoneNumber, SubscriptionStatus status);

    boolean existsByPhoneNumber(String phoneNumber);
}
