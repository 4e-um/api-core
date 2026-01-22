package com.project.notification.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notification.controller.dto.request.MessageLogSearchRequest;
import com.project.notification.controller.dto.response.MessageLogDetailResponse;
import com.project.notification.controller.dto.response.MessageLogResponse;
import com.project.notification.service.MessageLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notification/message-logs")
@RequiredArgsConstructor
public class MessageLogController {

    private final MessageLogService messageLogService;

    /**
     * 발송 로그 목록 조회 GET /notification/message-logs
     *
     * @param condition 검색 조건 (traceId, subId, channel, status, from, to)
     * @param pageable 페이징 정보 (기본값: 20개씩, sentAt 최신순)
     */
    @GetMapping
    public ResponseEntity<Slice<MessageLogResponse>> searchLogs(
            @ModelAttribute MessageLogSearchRequest condition,
            @PageableDefault(size = 20, sort = "sentAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        Slice<MessageLogResponse> result = messageLogService.searchLogs(condition, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 발송 로그 상세 조회 GET /notification/message-logs/{logId}
     *
     * @param logId 로그 ID (PK)
     */
    @GetMapping("/{logId}")
    public ResponseEntity<MessageLogDetailResponse> getLogDetail(
            @PathVariable(name = "logId") Long logId) {
        MessageLogDetailResponse detail = messageLogService.getLogDetail(logId);
        return ResponseEntity.ok(detail);
    }
}
