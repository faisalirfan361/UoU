package com.UoU.app.v1.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DiagnosticStatus")
public enum DiagnosticStatusDto {
  @JsonProperty("pending") PENDING,
  @JsonProperty("processing") PROCESSING,
  @JsonProperty("succeeded") SUCCEEDED,
  @JsonProperty("failed") FAILED,
}
