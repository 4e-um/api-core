package com.project.controller;

import com.project.common.dto.ApiInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "API 서버 기본 정보")
@RestController
public class HomeController {

    @Value("${project.name:Backend API}")
    private String projectName;

    @Value("${project.version:1.0.0}")
    private String projectVersion;

    @Operation(
            summary = "API 기본 엔드포인트",
            description = "API 서버의 상태 및 기본 정보를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "정상 응답",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiInfoResponse.class)
            )
    )
    @GetMapping("/")
    public ApiInfoResponse home() {
        return new ApiInfoResponse(
                "ok",
                projectName + " is running",
                projectVersion,
                "/swagger-ui.html"
        );
    }
}
