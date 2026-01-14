package com.project.core.controller;

import com.project.core.controller.dto.request.PlanChangeRequest;
import com.project.core.controller.dto.request.SubscriptionJoinRequest;
import com.project.core.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plan")
public class PlanController {

  private final PlanService planService;

  @PostMapping("/join")
  public ResponseEntity<Void> joinSubscription(@RequestBody SubscriptionJoinRequest request) {
    planService.joinSubscription(request.customerId(), request.planId());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/change")
  public ResponseEntity<Void> changePlan(@RequestBody PlanChangeRequest request) {
    planService.changePlan(request.subId(), request.planId());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{subId}/terminate")
  public ResponseEntity<Void> terminateSubscription(@PathVariable Long subId) {
    planService.terminateSubscription(subId);
    return ResponseEntity.ok().build();
  }
}
