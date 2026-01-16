package com.project.core.infra.repository.micro;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.core.infra.entity.micro.MicroPayment;

public interface MicroPaymentRepository extends JpaRepository<MicroPayment, Long> {}
