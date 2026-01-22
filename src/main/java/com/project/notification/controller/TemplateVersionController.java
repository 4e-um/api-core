package com.project.notification.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.notification.controller.dto.request.TemplateVersionCreateRequest;
import com.project.notification.controller.dto.request.TemplateVersionPreviewRequest;
import com.project.notification.controller.dto.request.TemplateVersionUpdateRequest;
import com.project.notification.controller.dto.response.TemplatePreviewResponse;
import com.project.notification.controller.dto.response.TemplateVersionListResponse;
import com.project.notification.controller.dto.response.TemplateVersionResponse;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.TemplateStatus;
import com.project.notification.service.TemplateVersionService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/notification/template-groups/{groupId}/versions")
@RequiredArgsConstructor
public class TemplateVersionController {

    private final TemplateVersionService templateVersionService;

    @PostMapping
    public ResponseEntity<TemplateVersionResponse> create(
            @PathVariable("groupId") Long groupId,
            @Valid @RequestBody TemplateVersionCreateRequest request) {
        TemplateVersionResponse response = templateVersionService.createVersion(groupId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TemplateVersionListResponse>> getVersions(
            @PathVariable("groupId") Long groupId,
            @RequestParam(name = "channel", required = false) Channel channel,
            @RequestParam(name = "status", required = false) TemplateStatus status,
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        Page<TemplateVersionListResponse> response =
                templateVersionService.getVersions(
                        groupId, channel, status, includeDeleted, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{versionId}")
    public ResponseEntity<TemplateVersionResponse> getVersion(
            @PathVariable("groupId") Long groupId, @PathVariable("versionId") Long versionId) {
        return ResponseEntity.ok(templateVersionService.getVersion(groupId, versionId));
    }

    @PatchMapping("/{versionId}")
    public ResponseEntity<TemplateVersionResponse> updateVersion(
            @PathVariable("groupId") Long groupId,
            @PathVariable("versionId") Long versionId,
            @RequestBody TemplateVersionUpdateRequest request) {
        return ResponseEntity.ok(templateVersionService.updateVersion(groupId, versionId, request));
    }

    @PostMapping("/{versionId}/activate")
    public ResponseEntity<TemplateVersionResponse> activateVersion(
            @PathVariable("groupId") Long groupId, @PathVariable("versionId") Long versionId) {
        return ResponseEntity.ok(templateVersionService.activateVersion(groupId, versionId));
    }

    @DeleteMapping("/{versionId}")
    public ResponseEntity<TemplateVersionResponse> deleteVersion(
            @PathVariable("groupId") Long groupId, @PathVariable("versionId") Long versionId) {
        return ResponseEntity.ok(templateVersionService.deleteVersion(groupId, versionId));
    }

    @PostMapping("/{versionId}/preview")
    public ResponseEntity<TemplatePreviewResponse> preview(
            @PathVariable("groupId") Long groupId,
            @PathVariable("versionId") Long versionId,
            @RequestBody TemplateVersionPreviewRequest request) {
        return ResponseEntity.ok(templateVersionService.preview(groupId, versionId, request));
    }
}
