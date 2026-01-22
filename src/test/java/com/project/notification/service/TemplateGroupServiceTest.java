package com.project.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.notification.controller.dto.response.TemplateGroupResponse;
import com.project.notification.infra.entity.TemplateGroup;
import com.project.notification.infra.repository.TemplateGroupRepository;
import com.project.notification.infra.repository.TemplateVersionRepository;

@ExtendWith(MockitoExtension.class)
class TemplateGroupServiceTest {

    @InjectMocks private TemplateGroupService templateGroupService;

    @Mock private TemplateGroupRepository groupRepository;
    @Mock private TemplateVersionRepository versionRepository;

    @Test
    @DisplayName("[삭제] 템플릿 그룹 삭제 시 버전 일괄 소프트 삭제")
    void deleteGroupShouldSoftDeleteVersions() {
        TemplateGroup group = org.mockito.Mockito.mock(TemplateGroup.class);
        given(groupRepository.findByIdAndIsDeletedFalse(1L)).willReturn(Optional.of(group));
        given(group.getId()).willReturn(1L);
        given(group.getCode()).willReturn("CODE_1");
        given(group.getName()).willReturn("Group");
        given(group.getDescription()).willReturn("Desc");
        given(group.isActive()).willReturn(false);
        given(group.isDeleted()).willReturn(true);
        given(group.getCreatedAt()).willReturn(LocalDateTime.now());
        given(group.getUpdatedAt()).willReturn(LocalDateTime.now());

        TemplateGroupResponse response = templateGroupService.delete(1L);

        verify(group).delete();
        verify(versionRepository).softDeleteByGroupId(1L);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.isDeleted()).isTrue();
    }
}
