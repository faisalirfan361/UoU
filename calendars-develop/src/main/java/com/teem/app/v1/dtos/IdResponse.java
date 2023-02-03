package com.UoU.app.v1.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic id response wrapper.
 *
 * <p>This is named without the "Dto" suffix because we can't use the @Schema annotation like normal
 * on generic classes or Sprindoc won't handle the generic variations correctly.
 */
public record IdResponse<T>(
    @Schema(required = true) T id
) {
}
