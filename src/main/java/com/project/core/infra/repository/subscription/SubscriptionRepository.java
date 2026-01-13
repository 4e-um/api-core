package com.project.core.infra.repository.subscription;

import com.project.core.infra.entity.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
