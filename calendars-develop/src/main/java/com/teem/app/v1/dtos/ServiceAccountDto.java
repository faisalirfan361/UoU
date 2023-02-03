package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "ServiceAccount", requiredProperties = SchemaExt.Required.ALL)
public record ServiceAccountDto(
    UUID id,
    String email,
    AuthMethodDto.ServiceAccount authMethod,
    Instant createdAt,
    @Schema(nullable = true) Instant updatedAt
) {
}

