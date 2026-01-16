package com.project.core.controller;

import com.project.core.controller.dto.request.MicroPaymentCancelRequest;
import com.project.core.controller.dto.request.MicroPaymentRequest;
import com.project.core.controller.dto.response.MicroPaymentResponse;
import com.project.core.service.MicroPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/micro-payment")
public class MicroPaymentController {

  private final MicroPaymentService microPaymentService;

  @PostMapping("/pay")
  public ResponseEntity<MicroPaymentResponse> pay(@Valid @RequestBody MicroPaymentRequest request) {
    MicroPaymentResponse response =
        microPaymentService.pay(request.subId(), request.name(), request.amount());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{microId}/cancel")
  public ResponseEntity<MicroPaymentResponse> cancel(
      @PathVariable Long microId, @Valid @RequestBody MicroPaymentCancelRequest request) {
    MicroPaymentResponse response = microPaymentService.cancel(microId, request.subId());
    return ResponseEntity.ok(response);
  }
}
