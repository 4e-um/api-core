package com.project.notification.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.core.controller.dto.response.MaskingUtil;
import com.project.global.exception.ApplicationException;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.OperationFailedException;
import com.project.global.util.AesUtil;
import com.project.notification.controller.dto.request.MessageLogSearchRequest;
import com.project.notification.controller.dto.response.MessageLogDetailResponse;
import com.project.notification.controller.dto.response.MessageLogResponse;
import com.project.notification.infra.entity.MessageLog;
import com.project.notification.infra.repository.MessageLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;
    private final AesUtil aesUtil;

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
                        .orElseThrow(() -> new ApplicationException(CoreErrorCode.LOG_NOT_FOUND));

        String decryptedRecipient = safeDecrypt(log.getRecipientEnc());
        String maskedRecipient = maskRecipient(decryptedRecipient);

        return MessageLogDetailResponse.from(log, maskedRecipient);
    }

    private String safeDecrypt(String encrypted) {
        if (encrypted == null) {
            return null;
        }
        try {
            return aesUtil.decrypt(encrypted);
        } catch (OperationFailedException e) {
            log.warn("Failed to decrypt recipient. value: {}", encrypted, e);
            return encrypted;
        }
    }

    private String maskRecipient(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            return recipient;
        }
        if (recipient.contains("@")) {
            return MaskingUtil.maskEmail(recipient);
        }
        return MaskingUtil.maskPhone(recipient);
    }
}
