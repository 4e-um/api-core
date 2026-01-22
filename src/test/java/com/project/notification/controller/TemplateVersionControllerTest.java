package com.project.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.notification.controller.dto.response.TemplatePreviewResponse;
import com.project.notification.controller.dto.response.TemplateVersionListResponse;
import com.project.notification.controller.dto.response.TemplateVersionResponse;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.infra.entity.enums.TemplateStatus;
import com.project.notification.service.TemplateVersionService;

@WebMvcTest(TemplateVersionController.class)
class TemplateVersionControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private TemplateVersionService templateVersionService;

    @Test
    @DisplayName("[생성] 템플릿 버전 생성 성공")
    void createVersionSuccess() throws Exception {
        TemplateVersionResponse response =
                new TemplateVersionResponse(
                        10L,
                        1L,
                        1,
                        Channel.EMAIL,
                        "Subject",
                        "Body",
                        Map.of("required", java.util.List.of("name")),
                        TemplateStatus.DRAFT,
                        false,
                        LocalDateTime.now(),
                        LocalDateTime.now());
        given(
                        templateVersionService.createVersion(
                                org.mockito.ArgumentMatchers.eq(1L),
                                org.mockito.ArgumentMatchers.any()))
                .willReturn(response);

        mockMvc.perform(
                        post("/notification/template-groups/1/versions")
                                .contentType("application/json")
                                .content(
                                        """
                                        {
                                          "channel": "EMAIL",
                                          "subject": "Subject",
                                          "body": "Body",
                                          "variables": { "required": ["name"] },
                                          "status": "DRAFT"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    @DisplayName("[생성] body 공백이면 검증 실패")
    void createVersionBlankBodyShouldFail() throws Exception {
        mockMvc.perform(
                        post("/notification/template-groups/1/versions")
                                .contentType("application/json")
                                .content(
                                        """
                                        {
                                          "channel": "EMAIL",
                                          "subject": "Subject",
                                          "body": ""
                                        }
                                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[조회] 템플릿 버전 목록 조회 성공")
    void getVersionsSuccess() throws Exception {
        TemplateVersionListResponse listResponse =
                new TemplateVersionListResponse(
                        10L,
                        1L,
                        1,
                        Channel.EMAIL,
                        "Subject",
                        TemplateStatus.DRAFT,
                        false,
                        LocalDateTime.now(),
                        LocalDateTime.now());
        PageImpl<TemplateVersionListResponse> page =
                new PageImpl<>(java.util.List.of(listResponse));
        given(
                        templateVersionService.getVersions(
                                org.mockito.ArgumentMatchers.eq(1L),
                                org.mockito.ArgumentMatchers.isNull(),
                                org.mockito.ArgumentMatchers.isNull(),
                                org.mockito.ArgumentMatchers.eq(false),
                                org.mockito.ArgumentMatchers.any()))
                .willReturn(page);

        mockMvc.perform(get("/notification/template-groups/1/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10L));
    }

    @Test
    @DisplayName("[수정] body 공백이면 검증 실패")
    void updateVersionBlankBodyShouldFail() throws Exception {
        mockMvc.perform(
                        patch("/notification/template-groups/1/versions/10")
                                .contentType("application/json")
                                .content(
                                        """
                                        {
                                          "body": ""
                                        }
                                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[프리뷰] 템플릿 프리뷰 성공")
    void previewSuccess() throws Exception {
        TemplatePreviewResponse response = new TemplatePreviewResponse("Subject", "Body");
        given(
                        templateVersionService.preview(
                                org.mockito.ArgumentMatchers.eq(1L),
                                org.mockito.ArgumentMatchers.eq(10L),
                                org.mockito.ArgumentMatchers.any()))
                .willReturn(response);

        mockMvc.perform(
                        post("/notification/template-groups/1/versions/10/preview")
                                .contentType("application/json")
                                .content(
                                        """
                                        {
                                          "variables": { "name": "Kim" }
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Subject"));
    }
}
