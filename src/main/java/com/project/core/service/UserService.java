package com.project.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
