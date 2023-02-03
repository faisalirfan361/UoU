package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import com.UoU.core.accounts.AccountConstraints;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SubaccountCreateRequest", requiredProperties = SchemaExt.Required.ALL)
public record SubaccountCreateRequestDto(
    String email,
    @Schema(maxLength = AccountConstraints.NAME_MAX) String name
) {
}
