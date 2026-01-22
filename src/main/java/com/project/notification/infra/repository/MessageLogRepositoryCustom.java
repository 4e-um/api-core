package com.project.notification.infra.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.project.notification.controller.dto.request.MessageLogSearchRequest;
import com.project.notification.infra.entity.MessageLog;

public interface MessageLogRepositoryCustom {
    Slice<MessageLog> searchLogs(MessageLogSearchRequest condition, Pageable pageable);

    Optional<MessageLog> findDetailById(Long id);
}
