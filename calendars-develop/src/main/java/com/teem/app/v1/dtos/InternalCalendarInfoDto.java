package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InternalCalendarInfo", requiredProperties = SchemaExt.Required.ALL)
public record InternalCalendarInfoDto(
    @Schema(example = "939c5172-ff35-453c-8832-ca7c072a58ca") String id,
    @Schema(example = "Desk 105") String name,
    @Schema(example = "939c5172-ff35-453c-8832-ca7c072a58ca-calendar@example.com") String email
) {
}
