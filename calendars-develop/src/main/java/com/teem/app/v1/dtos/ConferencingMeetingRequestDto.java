
package com.UoU.app.v1.dtos;

import com.UoU.core.conferencing.ConferencingConstraints;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "ConferencingMeetingRequest")
public record ConferencingMeetingRequestDto(
    // Note: autoCreate is used in case we want to accept manually-created meeting details later.
    // This naming is also similar to the Nylas API and makes the functionality a bit clearer.
    @Schema(nullable = true, description = "Auto create a conferencing meeting for the event")
    ConferencingMeetingAutoCreate autoCreate
) {

  public record ConferencingMeetingAutoCreate(
      @Schema(
          required = true,
          description = "Pre-authorized conferencing user for which the meeting will be created")
      UUID userId,

      @Schema(
          nullable = true,
          maxLength = ConferencingConstraints.LANGUAGE_MAX,
          description = "Optional language tag to use when requesting conferencing join info",
          example = "fr-CA")
      String language
  ) {
  }
}
