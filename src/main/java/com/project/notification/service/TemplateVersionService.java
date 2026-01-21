package com.project.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateVersionService {

    //    private final TemplateGroupRepository groupRepository;
    //    private final TemplateVersionRepository versionRepository;
    //
    //    // 템플릿 버전 생성
    //    @Transactional
    //    public Long createVersion(Long groupId, TemplateVersionCreateRequest request) {
    //        TemplateGroup group = groupRepository.findById(groupId)
    //                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
    //
    //        // 1. Max Version 조회 + 1
    //        int nextVersion = versionRepository.findMaxVersion(groupId, request.getChannel()) + 1;
    //
    //        // 2. Entity 생성 (기본 status = DRAFT)
    //        TemplateVersion version = TemplateVersion.builder()
    //                .templateGroup(group)
    //                .version(nextVersion)
    //                .channel(request.getChannel())
    //                .subject(request.getSubject())
    //                .body(request.getBody())
    //                .variables(request.getVariables())
    //                .build();
    //
    //        return versionRepository.save(version).getId();
    //    }
    //
    //    // 템플릿 활성화 (DRAFT -> ACTIVE)
    //    @Transactional
    //    public void activateVersion(Long groupId, Long versionId) {
    //        TemplateVersion targetVersion = versionRepository.findById(versionId)
    //                .orElseThrow(() -> new EntityNotFoundException("Version not found"));
    //
    //        // 검증: 그룹 ID 일치 여부 등
    //
    //        // 1. 기존 ACTIVE가 있다면 DRAFT로 강등 (Swap)
    //        versionRepository.findActiveVersion(groupId, targetVersion.getChannel())
    //                .ifPresent(TemplateVersion::deactivate); // Dirty Checking
    //
    //        // 2. 대상 버전 ACTIVE로 승격
    //        targetVersion.activate();
    //    }
}
