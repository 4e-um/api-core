package com.project.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.project.notification.controller.dto.response.TemplateGroupDetailResponse;
import com.project.notification.controller.dto.response.TemplateGroupResponse;
import com.project.notification.controller.dto.response.TemplateGroupResponse.ActiveTemplateSummary;
import com.project.notification.infra.entity.enums.Channel;
import com.project.notification.service.TemplateGroupService;

@WebMvcTest(TemplateGroupController.class)
class TemplateGroupControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private TemplateGroupService templateGroupService;

    @Test
    @DisplayName("[생성] 템플릿 그룹 생성 성공")
    void createGroupSuccess() throws Exception {
        TemplateGroupResponse response =
                new TemplateGroupResponse(
                        1L,
                        "CODE_1",
                        "Group Name",
                        "Desc",
                        true,
                        false,
                        LocalDateTime.now(),
                        LocalDateTime.now());
        given(templateGroupService.create(org.mockito.ArgumentMatchers.any())).willReturn(response);

        mockMvc.perform(
                        post("/notification/template-groups")
                                .with(csrf())
                                .contentType("application/json")
                                .content(
                                        """
                                        {
                                          "code": "CODE_1",
                                          "name": "Group Name",
                                          "description": "Desc",
                                          "isActive": true
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("CODE_1"));
    }

    @Test
    @DisplayName("[조회] 템플릿 그룹 목록 조회 성공")
    void getGroupsSuccess() throws Exception {
        TemplateGroupResponse response =
                new TemplateGroupResponse(
                        1L,
                        "CODE_1",
                        "Group Name",
                        "Desc",
                        true,
                        false,
                        LocalDateTime.now(),
                        LocalDateTime.now());
        PageImpl<TemplateGroupResponse> page = new PageImpl<>(List.of(response));
        given(
                        templateGroupService.getGroups(
                                org.mockito.ArgumentMatchers.isNull(),
                                org.mockito.ArgumentMatchers.eq(false),
                                org.mockito.ArgumentMatchers.isNull(),
                                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/notification/template-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("[조회] 템플릿 그룹 상세 조회 성공")
    void getGroupDetailSuccess() throws Exception {
        TemplateGroupDetailResponse response =
                new TemplateGroupDetailResponse(
                        1L,
                        "CODE_1",
                        "Group Name",
                        "Desc",
                        true,
                        false,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        Map.of(Channel.EMAIL, new ActiveTemplateSummary(10L, 1)));
        given(templateGroupService.getGroup(1L)).willReturn(response);

        mockMvc.perform(get("/notification/template-groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.activeTemplates.EMAIL.templateVersionId").value(10L));
    }

    @Test
    @DisplayName("[수정] name 공백이면 검증 실패")
    void updateGroupBlankNameShouldFail() throws Exception {
        mockMvc.perform(
                        patch("/notification/template-groups/1")
                                .with(csrf())
                                .contentType("application/json")
                                .content(
                                        """
                                        {
                                          "name": ""
                                        }
                                        """))
                .andExpect(status().isBadRequest());
    }
}
