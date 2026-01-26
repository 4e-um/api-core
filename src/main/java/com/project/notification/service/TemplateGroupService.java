package com.project.notification.service;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;
import com.project.notification.controller.dto.request.TemplateGroupCreateRequest;
import com.project.notification.controller.dto.request.TemplateGroupUpdateRequest;
import com.project.notification.controller.dto.response.TemplateGroupDetailResponse;
import com.project.notification.controller.dto.response.TemplateGroupResponse;
import com.project.notification.controller.dto.response.TemplateGroupResponse.ActiveTemplateSummary;
import com.project.notification.infra.entity.TemplateGroup;
import com.project.notification.infra.entity.TemplateVersion;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.TemplateStatus;
import com.project.notification.infra.repository.TemplateGroupRepository;
import com.project.notification.infra.repository.TemplateVersionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateGroupService {

    private final TemplateGroupRepository groupRepository;
    private final TemplateVersionRepository versionRepository;

    // 그룹 생성
    @Transactional
    public TemplateGroupResponse create(TemplateGroupCreateRequest request) {
        if (groupRepository.existsByCode(request.code())) {
            throw new InvalidStateException(CoreErrorCode.TEMPLATE_GROUP_CODE_DUPLICATED);
        }

        TemplateGroup group =
                TemplateGroup.builder()
                        .code(request.code())
                        .name(request.name())
                        .description(request.description())
                        .isActive(request.isActive() == null || request.isActive())
                        .build();

        TemplateGroup saved = groupRepository.save(group);
        return TemplateGroupResponse.from(saved);
    }

    // 그룹 목록 조회
    public Page<TemplateGroupResponse> getGroups(
            Boolean isActive, boolean includeDeleted, String keyword, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? "" : keyword;
        return groupRepository
                .search(isActive, includeDeleted, normalizedKeyword, pageable)
                .map(TemplateGroupResponse::from);
    }

    // 그룹 상세 조회
    public TemplateGroupDetailResponse getGroup(Long groupId) {
        TemplateGroup group =
                groupRepository
                        .findByIdAndIsDeletedFalse(groupId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.TEMPLATE_GROUP_NOT_FOUND));

        Map<Channel, ActiveTemplateSummary> activeTemplates =
                versionRepository.findAllByTemplateGroupId(groupId).stream()
                        .filter(v -> v.getStatus() == TemplateStatus.ACTIVE && !v.isDeleted())
                        .collect(
                                Collectors.toMap(
                                        TemplateVersion::getChannel,
                                        ActiveTemplateSummary::from,
                                        (v1, v2) -> v1,
                                        () -> new EnumMap<>(Channel.class)));

        return TemplateGroupDetailResponse.from(group, activeTemplates);
    }

    // 그룹 수정
    @Transactional
    public TemplateGroupResponse update(Long groupId, TemplateGroupUpdateRequest request) {
        TemplateGroup group =
                groupRepository
                        .findByIdAndIsDeletedFalse(groupId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.TEMPLATE_GROUP_NOT_FOUND));

        group.update(request.name(), request.description(), request.isActive());
        return TemplateGroupResponse.from(group);
    }

    // 그룹 삭제 (Soft Delete)
    @Transactional
    public TemplateGroupResponse delete(Long groupId) {
        TemplateGroup group =
                groupRepository
                        .findByIdAndIsDeletedFalse(groupId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.TEMPLATE_GROUP_NOT_FOUND));

        group.delete();
        versionRepository.softDeleteByGroupId(groupId);

        return TemplateGroupResponse.from(group);
    }
}
