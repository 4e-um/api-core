package com.project.core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.controller.dto.request.ChangeEmailRequest;
import com.project.core.controller.dto.request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.infra.entity.customer.Customer;
import com.project.core.infra.repository.customer.CustomerRepository;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class CustomerService {
	private final CustomerRepository customerRepository;
	
	@Transactional
	public List<Customer> loadByContactEnc(String contactEnc) {//유저 조회
		List<Customer> customers = customerRepository.findByContactEnc(contactEnc);
		if(customers.isEmpty()) {
      throw new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND);

		}
		return customers;
	}
	
	@Transactional
	public ChangeEmailResponse changeEmailEnc(Long userId, ChangeEmailRequest request) {//이메일 변경
		Customer customer = customerRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

		customer.changeEmailEnc(request.emailEnc());
	    return new ChangeEmailResponse(customer.getEmailEnc());
	}
	
	@Transactional
	public ChangeGradeResponse changeUserGrade(Long userId, ChangeGradeRequest request) {//등급 변경
		Customer customer = customerRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException(CoreErrorCode.CUSTOMER_NOT_FOUND));

		customer.changeGrade(request.grade());
	    return new ChangeGradeResponse(customer.getGrade());
	}
}
