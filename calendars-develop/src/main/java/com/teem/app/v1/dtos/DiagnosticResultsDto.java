package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(name = "DiagnosticResults", requiredProperties = SchemaExt.Required.ALL)
public record DiagnosticResultsDto(
    String calendarId,
    UUID runId,
    DiagnosticStatusDto status,
    @Schema(nullable = true) Instant startedAt,
    @Schema(nullable = true) Instant finishedAt,
    @Schema(nullable = true) Long durationSeconds,
    Instant expiresAt,
    List<DiagnosticEventDto> events
) {
}
