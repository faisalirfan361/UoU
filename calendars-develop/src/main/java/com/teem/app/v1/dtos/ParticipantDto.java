package com.UoU.app.v1.dtos;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.UoU.app.docs.SchemaExt;
import com.UoU.core.events.EventConstraints;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(name = "Participant", requiredProperties = {SchemaExt.Required.EXCEPT, "name"})
public record ParticipantDto(
    @Schema(nullable = true, maxLength = EventConstraints.PARTICIPANT_NAME_MAX) String name,
    String email,

    // These are only set from the provider side and synced to us, not set on our side:
    @Schema(accessMode = READ_ONLY, nullable = true) Status status,
    @Schema(accessMode = READ_ONLY, nullable = true) String comment
) {
  public enum Status {
    @JsonProperty("noreply") NO_REPLY("noreply"),
    @JsonProperty("yes") YES("yes"),
    @JsonProperty("no") NO("no"),
    @JsonProperty("maybe") MAYBE("maybe");

    @Getter
    private final String value;

    Status(String value) {
      this.value = value;
    }
  }
}
