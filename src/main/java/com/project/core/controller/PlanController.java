package com.project.core.controller;

import com.project.core.controller.dto.request.PlanChangeRequest;
import com.project.core.controller.dto.request.SubscriptionJoinRequest;
import com.project.core.controller.dto.response.PlanChangeResponse;
import com.project.core.controller.dto.response.SubscriptionJoinResponse;
import com.project.core.controller.dto.response.SubscriptionTerminateResponse;
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
  public ResponseEntity<SubscriptionJoinResponse> joinSubscription(
      @RequestBody SubscriptionJoinRequest request) {
    SubscriptionJoinResponse response =
        planService.joinSubscription(request.customerId(), request.planId());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/change")
  public ResponseEntity<PlanChangeResponse> changePlan(@RequestBody PlanChangeRequest request) {
    PlanChangeResponse response = planService.changePlan(request.subId(), request.planId());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{subId}/terminate")
  public ResponseEntity<SubscriptionTerminateResponse> terminateSubscription(
      @PathVariable Long subId) {
    SubscriptionTerminateResponse response = planService.terminateSubscription(subId);
    return ResponseEntity.ok(response);
  }
}
