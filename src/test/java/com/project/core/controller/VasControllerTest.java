package com.project.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.controller.dto.VasDto;
import com.project.core.infra.entity.vas.enums.VasStatus;
import com.project.core.service.VasService;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;

@WebMvcTest(VasController.class)
class VasControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private VasService vasService;

    @Test
    @DisplayName("[조회/성공] 부가서비스 이력 조회 성공")
    void getVasHistorySuccess() throws Exception {
        Long subId = 1L;
        VasDto.HistoryResponse res1 =
                new VasDto.HistoryResponse(
                        1L,
                        "Vas A",
                        1000,
                        VasStatus.TERMINATED.name(),
                        LocalDateTime.now().minusDays(30),
                        LocalDateTime.now());
        VasDto.HistoryResponse res2 =
                new VasDto.HistoryResponse(
                        2L, "Vas B", 2000, VasStatus.ACTIVE.name(), LocalDateTime.now(), null);

        when(vasService.getVasHistory(subId)).thenReturn(List.of(res2, res1));

        mockMvc.perform(get("/subscriptions/{subId}/vas", subId).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].vasName").value("Vas B"))
                .andExpect(jsonPath("$[1].vasName").value("Vas A"));
    }

    @Test
    @DisplayName("[가입/성공] 부가서비스 가입 요청 성공")
    void joinVasSuccess() throws Exception {
        Long subId = 1L;
        VasDto.Request request = new VasDto.Request(1L);
        VasDto.JoinResponse response =
                new VasDto.JoinResponse(10L, 1L, 1L, VasStatus.ACTIVE.name(), LocalDateTime.now());

        when(vasService.joinVas(eq(subId), any(Long.class))).thenReturn(response);

        mockMvc.perform(
                        post("/subscriptions/{subId}/vas", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subVasId").value(10L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("[가입/실패] 존재하지 않는 회선으로 가입 시도 시 404 반환")
    void joinVasFailSubNotFound() throws Exception {
        Long subId = 999L;
        VasDto.Request request = new VasDto.Request(1L);
        doThrow(new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND))
                .when(vasService)
                .joinVas(eq(subId), any(Long.class));

        mockMvc.perform(
                        post("/subscriptions/{subId}/vas", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(
                        status().is(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.title").value(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("[가입/실패] 이미 해지된 회선으로 가입 시도 시 400 반환")
    void joinVasFailSubTerminated() throws Exception {
        Long subId = 1L;
        VasDto.Request request = new VasDto.Request(1L);
        doThrow(new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED))
                .when(vasService)
                .joinVas(eq(subId), any(Long.class));

        mockMvc.perform(
                        post("/subscriptions/{subId}/vas", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(
                        status().is(
                                        CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED
                                                .getHttpStatus()
                                                .value()))
                .andExpect(
                        jsonPath("$.title")
                                .value(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED.name()));
    }

    @Test
    @DisplayName("[가입/실패] 존재하지 않는 부가서비스로 가입 시도 시 404 반환")
    void joinVasFailVasNotFound() throws Exception {
        Long subId = 1L;
        VasDto.Request request = new VasDto.Request(999L);
        doThrow(new EntityNotFoundException(CoreErrorCode.VAS_NOT_FOUND))
                .when(vasService)
                .joinVas(eq(subId), any(Long.class));

        mockMvc.perform(
                        post("/subscriptions/{subId}/vas", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(CoreErrorCode.VAS_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.title").value(CoreErrorCode.VAS_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("[가입/실패] 이미 가입된 부가서비스 가입 시도 시 400 반환")
    void joinVasFailAlreadySubscribed() throws Exception {
        Long subId = 1L;
        VasDto.Request request = new VasDto.Request(1L);
        doThrow(new InvalidStateException(CoreErrorCode.VAS_ALREADY_SUBSCRIBED))
                .when(vasService)
                .joinVas(eq(subId), any(Long.class));

        mockMvc.perform(
                        post("/subscriptions/{subId}/vas", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(
                        status().is(CoreErrorCode.VAS_ALREADY_SUBSCRIBED.getHttpStatus().value()))
                .andExpect(jsonPath("$.title").value(CoreErrorCode.VAS_ALREADY_SUBSCRIBED.name()));
    }

    @Test
    @DisplayName("[해지/성공] 부가서비스 해지 요청 성공")
    void terminateVasSuccess() throws Exception {
        Long subId = 1L;
        Long vasId = 1L;
        VasDto.TerminateResponse response =
                new VasDto.TerminateResponse(
                        10L, subId, 1L, VasStatus.TERMINATED.name(), LocalDateTime.now());

        when(vasService.terminateVas(eq(subId), eq(vasId))).thenReturn(response);

        mockMvc.perform(delete("/subscriptions/{subId}/vas/{vasId}", subId, vasId).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TERMINATED"));
    }

    @Test
    @DisplayName("[해지/실패] 존재하지 않는 회선에서 해지 시도 시 404 반환")
    void terminateVasFailSubNotFound() throws Exception {
        Long subId = 999L;
        Long vasId = 1L;
        doThrow(new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND))
                .when(vasService)
                .terminateVas(eq(subId), eq(vasId));

        mockMvc.perform(delete("/subscriptions/{subId}/vas/{vasId}", subId, vasId).with(csrf()))
                .andDo(print())
                .andExpect(
                        status().is(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.title").value(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("[해지/실패] 가입되지 않았거나 이미 해지된 부가서비스 해지 시도 시 400 반환")
    void terminateVasFailAlreadyTerminated() throws Exception {
        Long subId = 1L;
        Long vasId = 1L;
        // EntityNotFoundException이지만 코드는 VAS_ALREADY_TERMINATED (400)
        doThrow(new EntityNotFoundException(CoreErrorCode.VAS_ALREADY_TERMINATED))
                .when(vasService)
                .terminateVas(eq(subId), eq(vasId));

        mockMvc.perform(delete("/subscriptions/{subId}/vas/{vasId}", subId, vasId).with(csrf()))
                .andDo(print())
                .andExpect(
                        status().is(CoreErrorCode.VAS_ALREADY_TERMINATED.getHttpStatus().value()))
                .andExpect(jsonPath("$.title").value(CoreErrorCode.VAS_ALREADY_TERMINATED.name()));
    }

    @Test
    @DisplayName("[일괄해지/성공] 부가서비스 일괄 해지 요청 성공")
    void terminateVasBulkSuccess() throws Exception {
        Long subId = 1L;
        VasDto.BulkRequest request = new VasDto.BulkRequest(List.of(1L, 2L));
        VasDto.BulkTerminateResponse response =
                new VasDto.BulkTerminateResponse(
                        subId,
                        2,
                        List.of(
                                new VasDto.TerminateResponse(
                                        10L,
                                        subId,
                                        1L,
                                        VasStatus.TERMINATED.name(),
                                        LocalDateTime.now()),
                                new VasDto.TerminateResponse(
                                        11L,
                                        subId,
                                        2L,
                                        VasStatus.TERMINATED.name(),
                                        LocalDateTime.now())));

        when(vasService.terminateVasBulk(eq(subId), anyList())).thenReturn(response);

        mockMvc.perform(
                        post("/subscriptions/{subId}/vas/bulk-terminate", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @DisplayName("[일괄해지/실패] 존재하지 않는 회선에서 일괄 해지 시도 시 404 반환")
    void terminateVasBulkFailSubNotFound() throws Exception {
        Long subId = 999L;
        VasDto.BulkRequest request = new VasDto.BulkRequest(List.of(1L, 2L));
        doThrow(new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND))
                .when(vasService)
                .terminateVasBulk(eq(subId), anyList());

        mockMvc.perform(
                        post("/subscriptions/{subId}/vas/bulk-terminate", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(
                        status().is(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.title").value(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("[일괄해지/실패] 해지할 부가서비스가 없을 때 404 반환")
    void terminateVasBulkFailVasNotFound() throws Exception {
        Long subId = 1L;
        VasDto.BulkRequest request = new VasDto.BulkRequest(List.of(1L, 2L));
        doThrow(new EntityNotFoundException(CoreErrorCode.VAS_NOT_FOUND))
                .when(vasService)
                .terminateVasBulk(eq(subId), anyList());

        mockMvc.perform(
                        post("/subscriptions/{subId}/vas/bulk-terminate", subId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(CoreErrorCode.VAS_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.title").value(CoreErrorCode.VAS_NOT_FOUND.name()));
    }
}
