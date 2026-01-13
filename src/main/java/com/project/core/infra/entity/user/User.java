package com.project.core.infra.entity.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.infra.entity.subscription.SubscriptionPlan;
import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.infra.entity.user.enums.Grade;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "customer")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "customer_id")
	private Long customerId;

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
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Subscription> subscriptionHistory = new ArrayList<>();

	
	public void changeEmailEnc(String emailEnc) {
		this.emailEnc = emailEnc;
	}
	public void changeGrade(Grade grade) {
		this.grade = grade;
	}
}
