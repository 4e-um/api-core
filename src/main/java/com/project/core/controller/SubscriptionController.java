package com.project.core.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.response.SubscriptionResponse;
import com.project.core.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByCustomer(
            @PathVariable(name = "customerId") Long customerId) {
        List<SubscriptionResponse> responses =
                subscriptionService.findSubscription(customerId).stream()
                        .map(SubscriptionResponse::from)
                        .toList();

        return ResponseEntity.ok(responses);
    }
}
