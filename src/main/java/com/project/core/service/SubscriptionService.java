package com.project.core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.util.AESUtil;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class SubscriptionService {
	
	private final SubscriptionRepository subscriptionRepository;
	private final AESUtil aesUtil;

	@Transactional
	public List<Subscription> findSubscription(Long customerId) throws Exception {
	    List<Subscription> subscriptions =
	            subscriptionRepository.findByCustomer_CustomerId(customerId);

	    if (subscriptions.isEmpty()) {
	        throw new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND);
	    }
	    
	    return subscriptions;
	}
}
