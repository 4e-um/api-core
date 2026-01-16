package com.project.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core.controller.dto.request.VasBulkTerminateRequest;
import com.project.core.controller.dto.request.VasJoinRequest;
import com.project.core.controller.dto.request.VasTerminateRequest;
import com.project.core.controller.dto.response.VasBulkTerminateResponse;
import com.project.core.controller.dto.response.VasJoinResponse;
import com.project.core.controller.dto.response.VasTerminateResponse;
import com.project.core.infra.entity.vas.enums.VasStatus;
import com.project.core.service.VasService;
import com.project.global.exception.code.domain.core.CoreErrorCode;
import com.project.global.exception.core.EntityNotFoundException;
import com.project.global.exception.core.InvalidStateException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VasController.class)
class VasControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private VasService vasService;

  @Test
  @DisplayName("[가입/성공] 부가서비스 가입 요청 성공")
  void joinVasSuccess() throws Exception {
    Long subId = 1L;
    VasJoinRequest request = new VasJoinRequest(1L);
    VasJoinResponse response =
        new VasJoinResponse(10L, 1L, 1L, VasStatus.ACTIVE.name(), LocalDateTime.now());

    when(vasService.joinVas(eq(subId), any(Long.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/vas/{subId}/join", subId)
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
    VasJoinRequest request = new VasJoinRequest(1L);
    doThrow(new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND))
        .when(vasService)
        .joinVas(eq(subId), any(Long.class));

    mockMvc
        .perform(
            post("/vas/{subId}/join", subId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.name()));
  }

  @Test
  @DisplayName("[가입/실패] 이미 해지된 회선으로 가입 시도 시 400 반환")
  void joinVasFailSubTerminated() throws Exception {
    Long subId = 1L;
    VasJoinRequest request = new VasJoinRequest(1L);
    doThrow(new InvalidStateException(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED))
        .when(vasService)
        .joinVas(eq(subId), any(Long.class));

    mockMvc
        .perform(
            post("/vas/{subId}/join", subId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(
            status().is(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.SUBSCRIPTION_ALREADY_TERMINATED.name()));
  }

  @Test
  @DisplayName("[가입/실패] 존재하지 않는 부가서비스로 가입 시도 시 404 반환")
  void joinVasFailVasNotFound() throws Exception {
    Long subId = 1L;
    VasJoinRequest request = new VasJoinRequest(999L);
    doThrow(new EntityNotFoundException(CoreErrorCode.VAS_NOT_FOUND))
        .when(vasService)
        .joinVas(eq(subId), any(Long.class));

    mockMvc
        .perform(
            post("/vas/{subId}/join", subId)
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
    VasJoinRequest request = new VasJoinRequest(1L);
    doThrow(new InvalidStateException(CoreErrorCode.VAS_ALREADY_SUBSCRIBED))
        .when(vasService)
        .joinVas(eq(subId), any(Long.class));

    mockMvc
        .perform(
            post("/vas/{subId}/join", subId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is(CoreErrorCode.VAS_ALREADY_SUBSCRIBED.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.VAS_ALREADY_SUBSCRIBED.name()));
  }

  @Test
  @DisplayName("[해지/성공] 부가서비스 해지 요청 성공")
  void terminateVasSuccess() throws Exception {
    Long subId = 1L;
    VasTerminateRequest request = new VasTerminateRequest(1L);
    VasTerminateResponse response =
        new VasTerminateResponse(10L, subId, 1L, VasStatus.TERMINATED.name(), LocalDateTime.now());

    when(vasService.terminateVas(eq(subId), any(Long.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/vas/{subId}/terminate", subId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("TERMINATED"));
  }

  @Test
  @DisplayName("[해지/실패] 존재하지 않는 회선에서 해지 시도 시 404 반환")
  void terminateVasFailSubNotFound() throws Exception {
    Long subId = 999L;
    VasTerminateRequest request = new VasTerminateRequest(1L);
    doThrow(new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND))
        .when(vasService)
        .terminateVas(eq(subId), any(Long.class));

    mockMvc
        .perform(
            post("/vas/{subId}/terminate", subId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.name()));
  }

  @Test
  @DisplayName("[해지/실패] 가입되지 않았거나 이미 해지된 부가서비스 해지 시도 시 400 반환")
  void terminateVasFailAlreadyTerminated() throws Exception {
    Long subId = 1L;
    VasTerminateRequest request = new VasTerminateRequest(1L);
    // EntityNotFoundException이지만 코드는 VAS_ALREADY_TERMINATED (400)
    doThrow(new EntityNotFoundException(CoreErrorCode.VAS_ALREADY_TERMINATED))
        .when(vasService)
        .terminateVas(eq(subId), any(Long.class));

    mockMvc
        .perform(
            post("/vas/{subId}/terminate", subId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is(CoreErrorCode.VAS_ALREADY_TERMINATED.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.VAS_ALREADY_TERMINATED.name()));
  }

  @Test
  @DisplayName("[일괄해지/성공] 부가서비스 일괄 해지 요청 성공")
  void terminateVasBulkSuccess() throws Exception {
    Long subId = 1L;
    VasBulkTerminateRequest request = new VasBulkTerminateRequest(List.of(1L, 2L));
    VasTerminateResponse res1 =
        new VasTerminateResponse(10L, subId, 1L, VasStatus.TERMINATED.name(), LocalDateTime.now());
    VasTerminateResponse res2 =
        new VasTerminateResponse(11L, subId, 2L, VasStatus.TERMINATED.name(), LocalDateTime.now());
    VasBulkTerminateResponse response = new VasBulkTerminateResponse(subId, 2, List.of(res1, res2));

    when(vasService.terminateVasBulk(eq(subId), anyList())).thenReturn(response);

    mockMvc
        .perform(
            post("/vas/{subId}/bulk-terminate", subId)
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
    VasBulkTerminateRequest request = new VasBulkTerminateRequest(List.of(1L, 2L));
    doThrow(new EntityNotFoundException(CoreErrorCode.SUBSCRIPTION_NOT_FOUND))
        .when(vasService)
        .terminateVasBulk(eq(subId), anyList());

    mockMvc
        .perform(
            post("/vas/{subId}/bulk-terminate", subId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.SUBSCRIPTION_NOT_FOUND.name()));
  }

  @Test
  @DisplayName("[일괄해지/실패] 해지할 부가서비스가 없을 때 404 반환")
  void terminateVasBulkFailVasNotFound() throws Exception {
    Long subId = 1L;
    VasBulkTerminateRequest request = new VasBulkTerminateRequest(List.of(1L, 2L));
    doThrow(new EntityNotFoundException(CoreErrorCode.VAS_NOT_FOUND))
        .when(vasService)
        .terminateVasBulk(eq(subId), anyList());

    mockMvc
        .perform(
            post("/vas/{subId}/bulk-terminate", subId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().is(CoreErrorCode.VAS_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.title").value(CoreErrorCode.VAS_NOT_FOUND.name()));
  }
}
