package com.project.core.service;

import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.util.AesUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final AesUtil aesUtil;

  @Transactional(readOnly = true)
  public List<Subscription> findSubscription(Long customerId) {

    List<Subscription> subscriptions = subscriptionRepository.findByCustomer_CustomerId(customerId);

    if (subscriptions.isEmpty()) {
      throw new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
    }

    return subscriptions;
  }
}
