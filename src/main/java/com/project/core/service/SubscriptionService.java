package com.project.core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.subscription.SubscriptionRepository;
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
	        throw new IllegalStateException("보유중인 회선이 없습니다.");
	    }else {
	    	//복호화 -> 마스킹
	    	for (int i = 0; i < subscriptions.size(); i++) {
				String num = aesUtil.decrypt(subscriptions.get(i).getPhoneNumber());
				String maskedNum = num.substring(0,4)+"**"+num.substring(6,9)+"**"+num.substring(11,12);	//"010-**34-**78"
				subscriptions.get(i).setPhoneNumber(maskedNum);
			}
	    }
	    
	    return subscriptions;
	}
}
