package com.project.example.controller.dto;

public record SaveExampleRequest(
        String exampleName,
        String exampleContent
) {
}
