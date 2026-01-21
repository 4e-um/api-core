package com.project.notification.controller.dto.request;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.MessageStatus;

public record LogSearchRequest(
        String traceId,
        Long subId,
        Channel channel,
        MessageStatus status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {}
