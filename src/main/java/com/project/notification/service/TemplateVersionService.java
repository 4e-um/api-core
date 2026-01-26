package com.project.notification.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;
import com.project.notification.controller.dto.request.TemplateVersionCreateRequest;
import com.project.notification.controller.dto.request.TemplateVersionPreviewRequest;
import com.project.notification.controller.dto.request.TemplateVersionUpdateRequest;
import com.project.notification.controller.dto.response.TemplatePreviewResponse;
import com.project.notification.controller.dto.response.TemplateVersionListResponse;
import com.project.notification.controller.dto.response.TemplateVersionResponse;
import com.project.notification.infra.entity.TemplateGroup;
import com.project.notification.infra.entity.TemplateVersion;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.TemplateStatus;
import com.project.notification.infra.repository.TemplateGroupRepository;
import com.project.notification.infra.repository.TemplateVersionRepository;

@Service
@Transactional(readOnly = true)
public class TemplateVersionService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");

    private final TemplateGroupRepository groupRepository;
    private final TemplateVersionRepository versionRepository;
    private final TemplateVersionService self;

    public TemplateVersionService(
            TemplateGroupRepository groupRepository,
            TemplateVersionRepository versionRepository,
            @Lazy TemplateVersionService self) {
        this.groupRepository = groupRepository;
        this.versionRepository = versionRepository;
        this.self = self;
    }

    // 템플릿 버전 생성
    @Transactional
    public TemplateVersionResponse createVersion(
            Long groupId, TemplateVersionCreateRequest request) {
        TemplateGroup group =
                groupRepository
                        .findByIdAndIsDeletedFalse(groupId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.TEMPLATE_GROUP_NOT_FOUND));

        validateChannelAndSubject(request.channel(), request.subject());
        validateBody(request.body());

        int nextVersion = versionRepository.findMaxVersion(groupId, request.channel()) + 1;

        String subject =  request.subject();
        TemplateVersion version =
                TemplateVersion.builder()
                        .templateGroup(group)
                        .version(nextVersion)
                        .channel(request.channel())
                        .subject(subject)
                        .body(request.body())
                        .variables(request.variables())
                        .build();

        TemplateVersion saved = versionRepository.save(version);

        if (request.status() == TemplateStatus.ACTIVE) {
            self.activateVersion(groupId, saved.getId());
        }

        return TemplateVersionResponse.from(saved);
    }

    // 버전 목록
    public Page<TemplateVersionListResponse> getVersions(
            Long groupId,
            Channel channel,
            TemplateStatus status,
            boolean includeDeleted,
            Pageable pageable) {
        Page<TemplateVersion> result =
                versionRepository.search(groupId, channel, status, includeDeleted, pageable);
        return result.map(TemplateVersionListResponse::from);
    }

    // 버전 상세
    public TemplateVersionResponse getVersion(Long groupId, Long versionId) {
        TemplateVersion version =
                versionRepository
                        .findByIdAndTemplateGroupId(versionId, groupId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.TEMPLATE_VERSION_NOT_FOUND));

        if (version.isDeleted()) {
            throw new EntityNotFoundException(CoreErrorCode.TEMPLATE_VERSION_NOT_FOUND);
        }

        return TemplateVersionResponse.from(version);
    }

    // 버전 수정
    @Transactional
    public TemplateVersionResponse updateVersion(
            Long groupId, Long versionId, TemplateVersionUpdateRequest request) {
        TemplateVersion version =
                versionRepository
                        .findByIdAndTemplateGroupId(versionId, groupId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.TEMPLATE_VERSION_NOT_FOUND));

        if (version.isDeleted()) {
            throw new EntityNotFoundException(CoreErrorCode.TEMPLATE_VERSION_NOT_FOUND);
        }
        if (version.getStatus() == TemplateStatus.ACTIVE) {
            throw new InvalidStateException(CoreErrorCode.TEMPLATE_VERSION_ACTIVE_CANNOT_UPDATE);
        }

        if (request.subject() != null) {
            validateChannelAndSubject(version.getChannel(), request.subject());
        }
        if (request.body() != null) {
            validateBody(request.body());
        }

        String subject = request.subject();
        version.updateContent(subject, request.body(), request.variables());

        return TemplateVersionResponse.from(version);
    }

    // 버전 삭제 (Soft Delete)
    @Transactional
    public TemplateVersionResponse deleteVersion(Long groupId, Long versionId) {
        TemplateVersion version =
                versionRepository
                        .findByIdAndTemplateGroupId(versionId, groupId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.TEMPLATE_VERSION_NOT_FOUND));

        if (version.getStatus() == TemplateStatus.ACTIVE) {
            throw new InvalidStateException(CoreErrorCode.TEMPLATE_VERSION_ACTIVE_CANNOT_DELETE);
        }

        version.delete();
        return TemplateVersionResponse.from(version);
    }

    // 템플릿 활성화 (DRAFT -> ACTIVE)
    @Transactional
    public TemplateVersionResponse activateVersion(Long groupId, Long versionId) {
        TemplateVersion targetVersion =
                versionRepository
                        .findByIdAndTemplateGroupId(versionId, groupId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.TEMPLATE_VERSION_NOT_FOUND));

        if (targetVersion.isDeleted()) {
            throw new EntityNotFoundException(CoreErrorCode.TEMPLATE_VERSION_NOT_FOUND);
        }

        if (targetVersion.getStatus() == TemplateStatus.ACTIVE) {
            return TemplateVersionResponse.from(targetVersion);
        }

        versionRepository
                .findActiveVersion(groupId, targetVersion.getChannel())
                .ifPresent(TemplateVersion::deactivate);

        targetVersion.activate();
        return TemplateVersionResponse.from(targetVersion);
    }

    // 템플릿 Preview
    public TemplatePreviewResponse preview(
            Long groupId, Long versionId, TemplateVersionPreviewRequest request) {
        TemplateVersion version =
                versionRepository
                        .findByIdAndTemplateGroupId(versionId, groupId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                CoreErrorCode.TEMPLATE_VERSION_NOT_FOUND));

        if (version.isDeleted()) {
            throw new EntityNotFoundException(CoreErrorCode.TEMPLATE_VERSION_NOT_FOUND);
        }

        Map<String, Object> variables =
                request.variables() == null ? Map.of() : request.variables();

        Set<String> requiredKeys = extractRequiredKeys(version.getVariables());
        Set<String> missing = new HashSet<>();
        for (String key : requiredKeys) {
            if (!variables.containsKey(key)) {
                missing.add(key);
            }
        }
        if (!missing.isEmpty()) {
            throw new InvalidStateException(
                    CoreErrorCode.TEMPLATE_VARIABLE_MISSING,
                    CoreErrorCode.TEMPLATE_VARIABLE_MISSING.getMessage() + " Missing: " + missing);
        }

        String renderedSubject = render(version.getSubject(), variables);
        String renderedBody = render(version.getBody(), variables);

        return new TemplatePreviewResponse(renderedSubject, renderedBody);
    }

    private void validateChannelAndSubject(Channel channel, String subject) {
        if (channel == null) {
            throw new InvalidStateException(CoreErrorCode.TEMPLATE_CHANNEL_REQUIRED);
        }
        if ((channel == Channel.EMAIL) && (subject == null || subject.isBlank())) {
            throw new InvalidStateException(CoreErrorCode.TEMPLATE_SUBJECT_REQUIRED);
        }
    }

    private void validateBody(String body) {
        if (body == null || body.isBlank()) {
            throw new InvalidStateException(CoreErrorCode.TEMPLATE_BODY_REQUIRED);
        }
    }

    private Set<String> extractRequiredKeys(Map<String, Object> variables) {
        if (variables == null) {
            return Set.of();
        }
        Object required = variables.get("required");
        if (required != null && !(required instanceof List<?>)) {
            throw new InvalidStateException(
                    CoreErrorCode.TEMPLATE_VARIABLE_MISSING,
                    CoreErrorCode.TEMPLATE_VARIABLE_MISSING.getMessage()
                            + " Invalid required type: "
                            + required.getClass().getSimpleName());
        }
        if (required instanceof List<?> list) {
            Set<String> result = new HashSet<>();
            for (Object item : list) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        return Set.of();
    }

    private String render(String template, Map<String, Object> variables) {
        if (template == null) {
            return null;
        }

        return VARIABLE_PATTERN
                .matcher(template)
                .replaceAll(
                        matchResult -> {
                            String key = matchResult.group(1);
                            Object value = variables.get(key);
                            return value != null ? value.toString() : "";
                        });
    }
}
