package com.project.core.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.MicroPaymentDto;
import com.project.core.service.MicroPaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscriptions/{subId}/micropayments")
public class MicroPaymentController {

    private final MicroPaymentService microPaymentService;

    // 소액결제 내역 조회
    @GetMapping
    public ResponseEntity<List<MicroPaymentDto.HistoryResponse>> getMicroPaymentHistory(
            @PathVariable(name = "subId") Long subId) {
        List<MicroPaymentDto.HistoryResponse> history =
                microPaymentService.getMicroPaymentHistory(subId);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    public ResponseEntity<MicroPaymentDto.Response> pay(
            @PathVariable(name = "subId") Long subId,
            @Valid @RequestBody MicroPaymentDto.Request request) {
        MicroPaymentDto.Response response =
                microPaymentService.pay(subId, request.name(), request.amount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{microId}/cancel")
    public ResponseEntity<MicroPaymentDto.Response> cancel(
            @PathVariable(name = "subId") Long subId,
            @PathVariable(name = "microId") Long microId) {
        MicroPaymentDto.Response response = microPaymentService.cancel(microId, subId);
        return ResponseEntity.ok(response);
    }
}
