package com.project.core.controller;

import com.project.core.controller.dto.response.SubscriptionDiscountResponse;
import com.project.core.infra.entity.discount.SubscriptionDiscount;
import com.project.core.service.DiscountService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/discounts")
@RequiredArgsConstructor
public class DiscountController {

  private final DiscountService discountService;

  /** 회선(subId)에 적용된 할인 목록 조회 GET /discounts/subscriptions/{subId} */
  @GetMapping("/subscriptions/{subId}")
  public ResponseEntity<List<SubscriptionDiscountResponse>> getDiscountsBySubscription(
      @PathVariable Long subId) {
    List<SubscriptionDiscount> discounts = discountService.loadRequiredBySubId(subId);

    List<SubscriptionDiscountResponse> response =
        discounts.stream().map(SubscriptionDiscountResponse::from).toList();

    return ResponseEntity.ok(response);
  }

  /** 회선(subId)에 할인 정책(discountId) 추가(적용) POST /discounts */
  @PostMapping
  public ResponseEntity<CreateDiscountResponse> addDiscount(
      @RequestBody CreateDiscountRequest request) {
    Long sdId = discountService.addDiscount(request.getSubId(), request.getDiscountId());
    return ResponseEntity.status(HttpStatus.CREATED).body(new CreateDiscountResponse(sdId));
  }

  /** 기존 할인(sdId)을 종료하고, 새로운 할인 정책(discountId)로 변경 PATCH /discounts/{sdId} */
  @PatchMapping("/{sdId}")
  public ResponseEntity<ChangeDiscountResponse> changeDiscount(
      @PathVariable Long sdId, @RequestBody ChangeDiscountRequest request) {
    Long newSdId = discountService.changeDiscount(request.getDiscountId(), sdId);
    return ResponseEntity.ok(new ChangeDiscountResponse(newSdId));
  }

  // ===== DTOs =====

  public static class CreateDiscountRequest {
    private Long subId;
    private Long discountId;

    public Long getSubId() {
      return subId;
    }

    public Long getDiscountId() {
      return discountId;
    }

    public void setSubId(Long subId) {
      this.subId = subId;
    }

    public void setDiscountId(Long discountId) {
      this.discountId = discountId;
    }
  }

  public static class CreateDiscountResponse {
    private final Long sdId;

    public CreateDiscountResponse(Long sdId) {
      this.sdId = sdId;
    }

    public Long getSdId() {
      return sdId;
    }
  }

  public static class ChangeDiscountRequest {
    private Long discountId;

    public Long getDiscountId() {
      return discountId;
    }

    public void setDiscountId(Long discountId) {
      this.discountId = discountId;
    }
  }

  public static class ChangeDiscountResponse {
    private final Long newSdId;

    public ChangeDiscountResponse(Long newSdId) {
      this.newSdId = newSdId;
    }

    public Long getNewSdId() {
      return newSdId;
    }
  }
}
