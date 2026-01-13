package com.project.core.infra.entity.user;

import java.time.LocalDateTime;


import com.project.core.infra.entity.user.enums.Grade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "contact_enc", nullable = false)	//암호화된 전화번호
	private String contactEnc;

	@Column(name = "email_enc", nullable = false)	//암호화된 이메일
	private String emailEnc;

	@Enumerated(EnumType.STRING)
	@Column(name = "grade", nullable = false, length = 20)
	private Grade grade;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted;
}
