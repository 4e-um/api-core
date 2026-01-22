package com.project.notification.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.global.exception.ApplicationException;
import com.project.global.exception.code.domain.notification.NotificationErrorCode;
import com.project.notification.controller.dto.request.MessageLogSearchRequest;
import com.project.notification.controller.dto.response.MessageLogDetailResponse;
import com.project.notification.controller.dto.response.MessageLogResponse;
import com.project.notification.infra.entity.MessageLog;
import com.project.notification.infra.repository.MessageLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;

    /** 로그 목록 검색 */
    public Slice<MessageLogResponse> searchLogs(
            MessageLogSearchRequest condition, Pageable pageable) {
        return messageLogRepository.searchLogs(condition, pageable).map(MessageLogResponse::from);
    }

    /** 로그 상세 조회 */
    public MessageLogDetailResponse getLogDetail(Long logId) {
        MessageLog log =
                messageLogRepository
                        .findDetailById(logId)
                        .orElseThrow(
                                () ->
                                        new ApplicationException(
                                                NotificationErrorCode.LOG_NOT_FOUND));

        return MessageLogDetailResponse.from(log);
    }
}
