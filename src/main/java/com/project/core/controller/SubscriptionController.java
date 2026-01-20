package com.project.core.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.request.PhoneSearchRequest;
import com.project.core.controller.dto.response.SubscriptionDetailResponse;
import com.project.core.controller.dto.response.SubscriptionListResponse;
import com.project.core.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /** 전체 회선 목록 조회 (페이징) 125만 건의 데이터를 고려하여 페이징 적용 및 BatchSize 최적화 활용 */
    @GetMapping
    public ResponseEntity<Page<SubscriptionListResponse>> getAllSubscriptions(
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        Page<SubscriptionListResponse> response = subscriptionService.getAllSubscriptions(pageable);
        return ResponseEntity.ok(response);
    }

    /** 회선 전화번호 기반 상세 조회 사용중인 요금제, 부가서비스, 할인, 소액결제 내역 포함 */
    @PostMapping("/search")
    public ResponseEntity<SubscriptionDetailResponse> searchSubscription(
            @RequestBody PhoneSearchRequest request) {
        SubscriptionDetailResponse response =
                subscriptionService.getSubscriptionDetailByPhone(request.phoneRaw());
        return ResponseEntity.ok(response);
    }
}
