package com.project.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.controller.dto.Request.ChangeEmailRequest;
import com.project.core.controller.dto.Request.ChangeGradeRequest;
import com.project.core.controller.dto.response.ChangeEmailResponse;
import com.project.core.controller.dto.response.ChangeGradeResponse;
import com.project.core.infra.entity.user.User;
import com.project.core.infra.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	
	@Transactional
	public User loadByContactEnc(String contactEnc) {
		return userRepository.findByContactEnc(contactEnc).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));
	}
	
	@Transactional
	public ChangeEmailResponse changeEmailEnc(Long userId, ChangeEmailRequest request) {
	    User user = userRepository.findById(userId).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));

	    user.changeEmailEnc(request.emailEnc());
	    return new ChangeEmailResponse(user.getEmailEnc());
	}
	
	@Transactional
	public ChangeGradeResponse ChangeUserGrade(Long userId, ChangeGradeRequest request) {
	    User user = userRepository.findById(userId).orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));

	    user.changeGrade(request.grade());
	    return new ChangeGradeResponse(user.getGrade());
	}
}
