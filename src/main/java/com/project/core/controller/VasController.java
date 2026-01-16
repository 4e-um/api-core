package com.project.core.controller;

import com.project.core.controller.dto.request.VasBulkTerminateRequest;
import com.project.core.controller.dto.request.VasJoinRequest;
import com.project.core.controller.dto.request.VasTerminateRequest;
import com.project.core.controller.dto.response.SubscriptionVasResponse;
import com.project.core.controller.dto.response.VasBulkTerminateResponse;
import com.project.core.controller.dto.response.VasJoinResponse;
import com.project.core.controller.dto.response.VasTerminateResponse;
import com.project.core.service.VasService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vas")
public class VasController {

    private final VasService vasService;

  // 부가서비스 이력 조회
  @GetMapping("/{subId}/history")
  public ResponseEntity<List<SubscriptionVasResponse>> getVasHistory(
      @PathVariable(name = "subId") Long subId) {
    List<SubscriptionVasResponse> history = vasService.getVasHistory(subId);
    return ResponseEntity.ok(history);
  }

  @PostMapping("/{subId}/join")
  public ResponseEntity<VasJoinResponse> joinVas(
      @PathVariable(name = "subId") Long subId, @RequestBody VasJoinRequest request) {
    VasJoinResponse response = vasService.joinVas(subId, request.vasId());
    return ResponseEntity.ok(response);
  }

    @PostMapping("/{subId}/terminate")
    public ResponseEntity<VasTerminateResponse> terminateVas(
            @PathVariable(name = "subId") Long subId, @RequestBody VasTerminateRequest request) {
        VasTerminateResponse response = vasService.terminateVas(subId, request.vasId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{subId}/bulk-terminate")
    public ResponseEntity<VasBulkTerminateResponse> terminateVasBulk(
            @PathVariable(name = "subId") Long subId,
            @RequestBody VasBulkTerminateRequest request) {
        VasBulkTerminateResponse response = vasService.terminateVasBulk(subId, request.vasIds());
        return ResponseEntity.ok(response);
    }
}
