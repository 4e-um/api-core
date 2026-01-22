package com.project.notification.infra.repository;

import static com.project.core.infra.entity.subscription.QSubscription.subscription;
import static com.project.notification.infra.entity.QMessageLog.messageLog;
import static com.project.notification.infra.entity.QTemplateGroup.templateGroup;
import static com.project.notification.infra.entity.QTemplateVersion.templateVersion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.project.notification.controller.dto.request.MessageLogSearchRequest;
import com.project.notification.infra.entity.MessageLog;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.MessageStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageLogRepositoryImpl implements MessageLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 목록 조회
    @Override
    public Slice<MessageLog> searchLogs(MessageLogSearchRequest condition, Pageable pageable) {

        // 데이터 조회 (limit + 1)
        List<MessageLog> content =
                queryFactory
                        .selectFrom(messageLog)
                        .where(
                                traceIdEq(condition.traceId()),
                                subIdEq(condition.subId()),
                                channelEq(condition.channel()),
                                statusEq(condition.status()),
                                sentAtBetween(condition.from(), condition.to()))
                        .orderBy(messageLog.sentAt.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize() + 1)
                        .fetch();

        // hasNext 판단
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    // 상세 조회
    // N+1 방지: 연관된 엔티티를 한 번에 로딩
    @Override
    public Optional<MessageLog> findDetailById(Long id) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(messageLog)
                        // 1. Subscription 조인
                        .leftJoin(messageLog.subscription, subscription)
                        .fetchJoin()
                        // 2. TemplateVersion 조인
                        .leftJoin(messageLog.templateVersion, templateVersion)
                        .fetchJoin()
                        // 3. TemplateGroup까지 조인 (Snapshot 만들기 위해 필요)
                        .leftJoin(templateVersion.templateGroup, templateGroup)
                        .fetchJoin()
                        .where(messageLog.id.eq(id))
                        .fetchOne());
    }

    // --- 동적 쿼리 조건들 (null이면 무시됨) ---

    private BooleanExpression traceIdEq(String traceId) {
        return traceId != null ? messageLog.traceId.eq(traceId) : null;
    }

    private BooleanExpression subIdEq(Long subId) {
        return subId != null ? messageLog.subscription.subId.eq(subId) : null;
    }

    private BooleanExpression channelEq(Channel channel) {
        return channel != null ? messageLog.channel.eq(channel) : null;
    }

    private BooleanExpression statusEq(MessageStatus status) {
        return status != null ? messageLog.status.eq(status) : null;
    }

    private BooleanExpression sentAtBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return null;
        }
        return messageLog.sentAt.between(from, to);
    }
}
