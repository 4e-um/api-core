package com.project.core.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.VasDto;
import com.project.core.service.VasService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscriptions/{subId}/vas")
public class VasController {

    private final VasService vasService;

    // 부가서비스 이력 조회
    @GetMapping
    public ResponseEntity<List<VasDto.HistoryResponse>> getVasHistory(
            @PathVariable(name = "subId") Long subId) {
        List<VasDto.HistoryResponse> history = vasService.getVasHistory(subId);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    public ResponseEntity<VasDto.JoinResponse> joinVas(
            @PathVariable(name = "subId") Long subId, @RequestBody VasDto.Request request) {
        VasDto.JoinResponse response = vasService.joinVas(subId, request.vasId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{vasId}")
    public ResponseEntity<VasDto.TerminateResponse> terminateVas(
            @PathVariable(name = "subId") Long subId, @PathVariable(name = "vasId") Long vasId) {
        VasDto.TerminateResponse response = vasService.terminateVas(subId, vasId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-terminate")
    public ResponseEntity<VasDto.BulkTerminateResponse> terminateVasBulk(
            @PathVariable(name = "subId") Long subId, @RequestBody VasDto.BulkRequest request) {
        VasDto.BulkTerminateResponse response =
                vasService.terminateVasBulk(subId, request.vasIds());
        return ResponseEntity.ok(response);
    }
}
