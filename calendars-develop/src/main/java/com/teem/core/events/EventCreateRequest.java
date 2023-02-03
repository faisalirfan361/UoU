package com.UoU.core.events;

import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.conferencing.ConferencingMeetingCreateRequest;
import java.time.Instant;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record EventCreateRequest(
    @Valid @NotNull EventId id,
    @Valid EventExternalId externalId,
    String icalUid,
    @Valid @NotNull OrgId orgId,
    @Valid @NotNull CalendarId calendarId,
    @Size(min = 1, max = EventConstraints.TITLE_MAX) String title, // Nylas requires min 1 char
    @Size(max = EventConstraints.DESCRIPTION_MAX) String description,
    @Size(max = EventConstraints.LOCATION_MAX) String location,
    @Valid @NotNull When when,
    @Valid Recurrence recurrence,
    Event.Status status,
    boolean isBusy,
    boolean isReadOnly,
    Instant checkinAt,
    Instant checkoutAt,
    @Valid Owner owner,
    @Valid List<ParticipantRequest> participants,
    @Valid ConferencingMeetingCreateRequest conferencing,
    @Valid DataSource dataSource
) {

  @lombok.Builder(builderClassName = "Builder", toBuilder = true)
  public EventCreateRequest {
    recurrence = recurrence != null ? recurrence : Recurrence.none();
  }
}
