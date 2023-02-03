package com.UoU.app.v1.dtos;

import com.UoU.core.DataConfig;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public record PageParamsDto(
    @Schema(nullable = true) String cursor,
    @Schema(nullable = true, maximum = DataConfig.Paging.MAX_LIMIT_STR) Integer limit) {

  public PageParamsDto {
    cursor = cursor == null || cursor.isBlank() ? null : cursor;
    limit = limit == null || limit == 0
        ? DataConfig.Paging.DEFAULT_LIMIT
        : Math.min(DataConfig.Paging.MAX_LIMIT, Math.max(1, limit));
  }

  /**
   * Annotation for controller methods to document how page params are pulled from the query.
   */
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @Parameter(
      in = ParameterIn.QUERY,
      name = "cursor",
      description = "Page cursor of the page to be returned "
          + "(get from **meta.nextCursor** of previous response)",
      schema = @Schema(type = "string", example = "ZXhhbXBsZS1jdXJzb3ItdmFsdWU="))
  @Parameter(
      in = ParameterIn.QUERY,
      name = "limit",
      description = "Page size to be returned (max: "
          + DataConfig.Paging.MAX_LIMIT_STR + ")",
      schema = @Schema(type = "integer", defaultValue = DataConfig.Paging.DEFAULT_LIMIT_STR))
  public @interface ParametersInQuery {
  }
}
