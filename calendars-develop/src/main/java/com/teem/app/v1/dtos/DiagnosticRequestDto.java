package com.UoU.app.v1.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DiagnosticRequest")
public record DiagnosticRequestDto(
    @Schema(required = true) String calendarId,
    @Schema(nullable = true) String callbackUri
) {
}
