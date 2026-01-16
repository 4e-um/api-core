package com.project.core.infra.repository.micro;

import com.project.core.infra.entity.micro.MicroPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MicroPaymentRepository extends JpaRepository<MicroPayment, Long> {}
