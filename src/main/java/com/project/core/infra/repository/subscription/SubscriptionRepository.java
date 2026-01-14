package com.project.core.infra.repository.subscription;

import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.discount.SubscriptionDiscount;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
	long countByCustomerAndStatus(Customer customer, SubscriptionStatus status);
	boolean existsByPhoneNumberAndStatus(String phoneNumber, SubscriptionStatus status);
	boolean existsByPhoneNumber(String phoneNumber);
	List<Subscription> findByCostomerId(Long customerId);
}
