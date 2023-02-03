package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Owner", requiredProperties = SchemaExt.Required.ALL)
public record OwnerDto(
    @Schema(nullable = true) String name,
    String email
) {
}
