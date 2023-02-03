package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "AccountError", requiredProperties = SchemaExt.Required.ALL)
public record AccountErrorDto(
    UUID id,
    Instant createdAt,
    Type type,
    String message
) {

  public enum Type {
    AUTH
  }
}
