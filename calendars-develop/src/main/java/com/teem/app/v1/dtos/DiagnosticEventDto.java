package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@Schema(name = "DiagnosticEvent", requiredProperties = SchemaExt.Required.ALL)
public record DiagnosticEventDto(
    Instant time,
    String name,
    String message,
    Map<String, Object> data,
    boolean isError
) {
}
