package com.UoU.app.v1.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "Account", requiredProperties = SchemaExt.Required.ALL)
public record AccountDto(
    String id,
    @Schema(nullable = true) UUID serviceAccountId,
    String email,
    String name,
    SyncStateDto syncState,
    AuthMethodDto.Account authMethod,
    Instant createdAt,
    Instant updatedAt
) {

  @Schema(name = "SyncState")
  public enum SyncStateDto {
    @JsonProperty("unknown") UNKNOWN,
    @JsonProperty("initializing") INITIALIZING,
    @JsonProperty("downloading") DOWNLOADING,
    @JsonProperty("running") RUNNING,
    @JsonProperty("partial") PARTIAL,
    @JsonProperty("invalid-credentials") INVALID_CREDENTIALS,
    @JsonProperty("exception") EXCEPTION,
    @JsonProperty("sync-error") SYNC_ERROR,
    @JsonProperty("stopped") STOPPED,
  }
}
