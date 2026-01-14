package com.project.core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.repository.customer.CustomerRepository;
import com.project.global.util.AESUtil;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class CustomerService {
	private final CustomerRepository customerRepository;
	private final AESUtil aesUtil;
	
	@Transactional
	public List<Customer> loadByContactEnc(String contactEnc) {//유저 조회
		List<Customer> customers = customerRepository.findByContactEnc(contactEnc);
		if(customers.isEmpty()) {
			throw new IllegalStateException("해당하는 사용자가 없습니다");
		}
		return customers;
	}
	
	@Transactional
	public ChangeEmailResponse changeEmailEnc(Long userId, ChangeEmailRequest request) throws Exception {//이메일 변경
		Customer customer = customerRepository.findById(userId).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
		String encEmail = aesUtil.encrypt(request.emailEnc());
		customer.changeEmailEnc(encEmail);
	    return new ChangeEmailResponse(encEmail);
	}
	
	@Transactional
	public ChangeGradeResponse changeUserGrade(Long userId, ChangeGradeRequest request) {//등급 변경
		Customer customer = customerRepository.findById(userId).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));

		customer.changeGrade(request.grade());
	    return new ChangeGradeResponse(customer.getGrade());
	}
}
