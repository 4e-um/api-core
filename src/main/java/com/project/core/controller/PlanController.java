package com.project.core.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.PlanDto;
import com.project.core.service.PlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscriptions")
public class PlanController {

    private final PlanService planService;

    // 요금제 이력 조회
    @GetMapping("/{subId}/plans")
    public ResponseEntity<List<PlanDto.HistoryResponse>> getPlanHistory(@PathVariable(name="subId") Long subId) {
        List<PlanDto.HistoryResponse> history = planService.getPlanHistory(subId);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    public ResponseEntity<PlanDto.JoinResponse> joinSubscription(
            @RequestBody PlanDto.JoinRequest request) {
        PlanDto.JoinResponse response =
                planService.joinSubscription(request.customerId(), request.planId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{subId}/plan")
    public ResponseEntity<PlanDto.ChangeResponse> changePlan(
            @PathVariable(name="subId") Long subId, @RequestBody PlanDto.ChangeRequest request) {
        PlanDto.ChangeResponse response = planService.changePlan(subId, request.planId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{subId}")
    public ResponseEntity<PlanDto.TerminateResponse> terminateSubscription(
            @PathVariable(name="subId") Long subId) {
        PlanDto.TerminateResponse response = planService.terminateSubscription(subId);
        return ResponseEntity.ok(response);
    }
}
