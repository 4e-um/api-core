package com.project.core.controller;

import com.project.core.infra.entity.subscription.Subscription;
import com.project.core.service.SubscriptionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscriptions")
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  /**
   * 유저(customerId)의 회선 목록 조회 - 성공: 200 + List - 없음: EntityNotFoundException -> ExceptionAdvice에서
   * 404 등으로 변환
   */
  @GetMapping("/customers/{customerId}")
  public ResponseEntity<List<Subscription>> getSubscriptionsByCustomer(
      @PathVariable(name = "customerId") Long customerId) {
    List<Subscription> subscriptions = subscriptionService.findSubscription(customerId);
    return ResponseEntity.ok(subscriptions);
  }
}
