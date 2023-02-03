package com.UoU.app.v1.dtos;

import com.UoU.core.auth.AuthConstraints;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthCodeCreateRequest")
public record AuthCodeCreateRequestDto(
    @Schema(nullable = true, maxLength = AuthConstraints.REDIRECT_URI_MAX) String redirectUri
) {
}
