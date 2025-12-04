package com.project.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "API 서버 기본 정보 응답")
@Getter
@AllArgsConstructor
public class ApiInfoResponse {

    @Schema(description = "API 서버 상태", example = "ok")
    private final String status;

    @Schema(description = "API 설명", example = "Backend API is running")
    private final String message;

    @Schema(description = "버전 정보", example = "1.0.0")
    private final String version;

    @Schema(description = "API 문서 경로", example = "/swagger-ui.html")
    private final String docs;
}
