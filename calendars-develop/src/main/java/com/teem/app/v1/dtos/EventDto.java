package com.UoU.app.v1.dtos;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Schema(
    name = "Event",
    requiredProperties = {SchemaExt.Required.EXCEPT, "recurrence", "recurrenceInstance"})
public record EventDto(
    UUID id,
    @Schema(nullable = true) String icalUid,
    String calendarId,
    @Schema(nullable = true) String title,
    @Schema(nullable = true) String description,
    @Schema(nullable = true) String location,
    WhenDto when,
    @JsonInclude(NON_NULL) RecurrenceDto recurrence,
    @JsonInclude(NON_NULL) RecurrenceInstanceDto recurrenceInstance,
    @Schema(nullable = true) Status status,
    boolean isBusy,
    boolean isReadOnly,
    @Schema(nullable = true) Instant checkinAt,
    @Schema(nullable = true) Instant checkoutAt,
    @Schema(nullable = true) OwnerDto owner,
    @Schema(nullable = true) List<ParticipantDto> participants,
    Instant createdAt,
    @Schema(nullable = true) String createdFrom,
    @Schema(nullable = true) Instant updatedAt,
    @Schema(nullable = true) String updatedFrom,
    @JsonInclude(NON_NULL) @Schema(hidden = true) DebugInfo debugInfo
) {

  /**
   * Event status.
   *
   * <p>Note that "cancelled" is a valid Nylas status but won't occur locally because we delete
   * cancelled events. We'll exclude it from the public contract for clarity.
   */
  public enum Status {
    @JsonProperty("confirmed") CONFIRMED("confirmed"),
    @JsonProperty("tentative") TENTATIVE("tentative");

    @Getter
    private final String value;

    Status(String value) {
      this.value = value;
    }
  }

  /**
   * Extra debug info that can be requested when needed but is hidden from API contract.
   *
   * <p>This is stuff that's safe for callers to see but is noise for most people. Don't put
   * anything in here that people actually shouldn't be able to see without extra permissions.
   */
  @Schema(hidden = true)
  public record DebugInfo(
      String externalId
  ) {
  }
}
