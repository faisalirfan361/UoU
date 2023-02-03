package com.UoU.app.v1.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthCodeResponse")
public record AuthCodeResponseDto(
    @Schema(required = true) String code
) {
}
