package com.project.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.response.UsageDashboardResponse;
import com.project.core.service.UsageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @GetMapping
    public ResponseEntity<UsageDashboardResponse> getUsageDashboard() {

        return ResponseEntity.ok(usageService.getDashboard());
    }
}
