package com.project.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.response.BillingDashboardResponse;
import com.project.core.service.InvoiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<BillingDashboardResponse> getBillingDashboard() {

        return ResponseEntity.ok(invoiceService.getDashboard());
    }
}
