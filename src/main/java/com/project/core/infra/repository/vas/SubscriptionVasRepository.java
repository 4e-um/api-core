package com.project.core.infra.repository.vas;

import com.project.core.infra.entity.subscription.enums.SubscriptionStatus;
import com.project.core.infra.entity.vas.SubscriptionVas;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionVasRepository extends JpaRepository<SubscriptionVas, Long> {
  // [중복 가입 방지용] 특정 회원이 해당 부가서비스를 ACTIVE 상태로 가지고 있는지 확인
  boolean existBySubscriptionSubIdAndVasVasIdAndStatus(
      Long subId, Long vasId, SubscriptionStatus status);

  // [해지용] 특정 회선의 특정 부가서비스 중 ACTIVE 상태인 것 조회
  Optional<SubscriptionVas> findBySubscriptionSubIdAndVasVasIdAndStatus(
      Long subId, Long vasId, SubscriptionStatus status);
}
