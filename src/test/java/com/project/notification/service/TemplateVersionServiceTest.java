package com.project.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.InvalidStateException;
import com.project.notification.controller.dto.request.TemplateVersionCreateRequest;
import com.project.notification.controller.dto.request.TemplateVersionPreviewRequest;
import com.project.notification.controller.dto.response.TemplateVersionResponse;
import com.project.notification.infra.entity.TemplateGroup;
import com.project.notification.infra.entity.TemplateVersion;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.TemplateStatus;
import com.project.notification.infra.repository.TemplateGroupRepository;
import com.project.notification.infra.repository.TemplateVersionRepository;

@ExtendWith(MockitoExtension.class)
class TemplateVersionServiceTest {

    @Mock private TemplateGroupRepository groupRepository;
    @Mock private TemplateVersionRepository versionRepository;
    @Mock private TemplateVersionService self;

    private TemplateVersionService templateVersionService;

    @BeforeEach
    void setup() {
        templateVersionService =
                new TemplateVersionService(groupRepository, versionRepository, self);
    }

    @Test
    @DisplayName("[생성] ACTIVE 요청 시 activateVersion을 프록시로 호출")
    void createVersionActiveShouldCallProxyActivate() {
        TemplateGroup group = org.mockito.Mockito.mock(TemplateGroup.class);
        given(groupRepository.findByIdAndIsDeletedFalse(1L)).willReturn(Optional.of(group));
        given(versionRepository.findMaxVersion(1L, Channel.EMAIL)).willReturn(0);

        TemplateVersion saved = org.mockito.Mockito.mock(TemplateVersion.class);
        given(versionRepository.save(org.mockito.ArgumentMatchers.any())).willReturn(saved);
        given(saved.getId()).willReturn(10L);
        given(saved.getTemplateGroup()).willReturn(group);
        given(saved.getVersion()).willReturn(1);
        given(saved.getChannel()).willReturn(Channel.EMAIL);
        given(saved.getSubject()).willReturn("Subject");
        given(saved.getBody()).willReturn("Body");
        given(saved.getVariables()).willReturn(Map.of());
        given(saved.getStatus()).willReturn(TemplateStatus.ACTIVE);
        given(saved.isDeleted()).willReturn(false);
        given(saved.getCreatedAt()).willReturn(LocalDateTime.now());
        given(saved.getUpdatedAt()).willReturn(LocalDateTime.now());
        given(group.getId()).willReturn(1L);

        TemplateVersionCreateRequest request =
                new TemplateVersionCreateRequest(
                        Channel.EMAIL, "Subject", "Body", Map.of(), TemplateStatus.ACTIVE);

        TemplateVersionResponse response = templateVersionService.createVersion(1L, request);

        verify(self).activateVersion(1L, 10L);
        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    @DisplayName("[프리뷰] 필수 변수 누락 시 상세 메시지 포함")
    void previewMissingRequiredVariablesShouldIncludeMissingList() {
        TemplateVersion version = org.mockito.Mockito.mock(TemplateVersion.class);
        given(versionRepository.findByIdAndTemplateGroupId(10L, 1L))
                .willReturn(Optional.of(version));
        given(version.isDeleted()).willReturn(false);
        given(version.getVariables()).willReturn(Map.of("required", List.of("name")));
        given(version.getSubject()).willReturn("Hi {{name}}");
        given(version.getBody()).willReturn("Body {{name}}");

        TemplateVersionPreviewRequest request =
                new TemplateVersionPreviewRequest(Map.of());

        assertThatThrownBy(() -> templateVersionService.preview(1L, 10L, request))
                .isInstanceOf(InvalidStateException.class)
                .extracting(Throwable::getMessage)
                .asString()
                .contains(CoreErrorCode.TEMPLATE_VARIABLE_MISSING.getMessage())
                .contains("Missing");
    }

    @Test
    @DisplayName("[프리뷰] required 타입이 List가 아니면 예외")
    void previewRequiredTypeInvalidShouldFail() {
        TemplateVersion version = org.mockito.Mockito.mock(TemplateVersion.class);
        given(versionRepository.findByIdAndTemplateGroupId(10L, 1L))
                .willReturn(Optional.of(version));
        given(version.isDeleted()).willReturn(false);
        given(version.getVariables()).willReturn(Map.of("required", "name"));
        given(version.getSubject()).willReturn("Hi {{name}}");
        given(version.getBody()).willReturn("Body {{name}}");

        TemplateVersionPreviewRequest request =
                new TemplateVersionPreviewRequest(Map.of("name", "Kim"));

        assertThatThrownBy(() -> templateVersionService.preview(1L, 10L, request))
                .isInstanceOf(InvalidStateException.class)
                .extracting(Throwable::getMessage)
                .asString()
                .contains("Invalid required type");
    }
}
