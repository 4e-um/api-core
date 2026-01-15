package com.project.core.infra.repository.discount;

import com.project.core.infra.entity.discount.SubscriptionDiscount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionDiscountRepository extends JpaRepository<SubscriptionDiscount, Long> {
  List<SubscriptionDiscount> findBySubscription_SubId(Long subId);

  Optional<SubscriptionDiscount> findBySdId(Long sdId);
}
