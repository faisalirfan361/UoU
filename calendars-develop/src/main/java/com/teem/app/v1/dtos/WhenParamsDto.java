package com.UoU.app.v1.dtos;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;

public record WhenParamsDto(
    @Schema(nullable = true) Instant startsBefore,
    @Schema(nullable = true) Instant startsAfter,
    @Schema(nullable = true) Instant endsBefore,
    @Schema(nullable = true) Instant endsAfter
) {

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @Parameter(
      in = ParameterIn.QUERY,
      name = "startsBefore",
      description = "Return only events that start before the specified date-time",
      example = "2022-11-02T14:00:00Z",
      schema = @Schema(type = "date-time"))
  @Parameter(
      in = ParameterIn.QUERY,
      name = "startsAfter",
      description = "Return only events that start after the specified date-time",
      example = "2022-11-01T14:00:00Z",
      schema = @Schema(type = "date-time"))
  @Parameter(
      in = ParameterIn.QUERY,
      name = "endsBefore",
      description = "Return only events that end before the specified date-time",
      example = "2022-11-02T14:00:00Z",
      schema = @Schema(type = "date-time"))
  @Parameter(
      in = ParameterIn.QUERY,
      name = "endsAfter",
      description = "Return only events that end after the specified date-time",
      example = "2022-11-01T14:00:00Z",
      schema = @Schema(type = "date-time"))
  public @interface ParametersInQuery {
  }
}
