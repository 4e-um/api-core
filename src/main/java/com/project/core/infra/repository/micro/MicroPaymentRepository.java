package com.project.core.infra.repository.micro;

import com.project.core.infra.entity.micro.MicroPayment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MicroPaymentRepository extends JpaRepository<MicroPayment, Long> {
  // 특정 회선의 소액결제 내역 전체 조회 (최신순)
  List<MicroPayment> findBySubscriptionSubIdOrderByPayDateDesc(Long subId);
}
