package com.project.core.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.request.PlanChangeRequest;
import com.project.core.controller.dto.request.SubscriptionJoinRequest;
import com.project.core.controller.dto.response.PlanChangeResponse;
import com.project.core.controller.dto.response.SubscriptionJoinResponse;
import com.project.core.controller.dto.response.SubscriptionPlanResponse;
import com.project.core.controller.dto.response.SubscriptionTerminateResponse;
import com.project.core.service.PlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plan")
public class PlanController {

    private final PlanService planService;

    // 요금제 이력 조회
    @GetMapping("/{subId}/history")
    public ResponseEntity<List<SubscriptionPlanResponse>> getPlanHistory(
            @PathVariable(name = "subId") Long subId) {
        List<SubscriptionPlanResponse> history = planService.getPlanHistory(subId);
        return ResponseEntity.ok(history);
    }

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
            @PathVariable(name = "subId") Long subId) {
        SubscriptionTerminateResponse response = planService.terminateSubscription(subId);
        return ResponseEntity.ok(response);
    }
}
