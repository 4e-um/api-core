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

import com.project.notification.controller.dto.request.TemplateGroupCreateRequest;
import com.project.notification.controller.dto.request.TemplateGroupUpdateRequest;
import com.project.notification.controller.dto.response.TemplateGroupDetailResponse;
import com.project.notification.controller.dto.response.TemplateGroupResponse;
import com.project.notification.service.TemplateGroupService;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class TemplateGroupController {

    private final TemplateGroupService templateGroupService;

    @PostMapping
    public ResponseEntity<TemplateGroupResponse> create(
            @Valid @RequestBody TemplateGroupCreateRequest request) {
        TemplateGroupResponse response = templateGroupService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TemplateGroupResponse>> getGroups(
            @RequestParam(name = "isActive", required = false) Boolean isActive,
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted,
            @RequestParam(name = "keyword", required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        Page<TemplateGroupResponse> response =
                templateGroupService.getGroups(isActive, includeDeleted, keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<TemplateGroupDetailResponse> getGroup(
            @PathVariable("groupId") Long groupId) {
        return ResponseEntity.ok(templateGroupService.getGroup(groupId));
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<TemplateGroupResponse> update(
            @PathVariable("groupId") Long groupId,
            @RequestBody TemplateGroupUpdateRequest request) {
        return ResponseEntity.ok(templateGroupService.update(groupId, request));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<TemplateGroupResponse> delete(@PathVariable("groupId") Long groupId) {
        return ResponseEntity.ok(templateGroupService.delete(groupId));
    }
}
