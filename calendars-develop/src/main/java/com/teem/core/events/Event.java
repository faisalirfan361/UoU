package com.UoU.core.events;

import com.UoU.core.Auditable;
import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Getter;

public record Event(
    @NotNull EventId id,
    EventExternalId externalId,
    String icalUid,
    @NotNull OrgId orgId,
    @NotNull CalendarId calendarId,
    String title,
    String description,
    String location,
    @NotNull When when,
    Recurrence recurrence,
    Status status,
    boolean isBusy,
    boolean isReadOnly,
    Instant checkinAt,
    Instant checkoutAt,
    Owner owner,
    List<Participant> participants,
    @NotNull Instant createdAt,
    DataSource createdFrom,
    Instant updatedAt,
    DataSource updatedFrom
)   implements Auditable {

  public Event {
    recurrence = recurrence != null ? recurrence : Recurrence.none();
  }

  public EventAccessInfo accessInfo() {
    return new EventAccessInfo(orgId, isReadOnly);
  }

  /**
   * Event status.
   *
   * <p>Note that "cancelled" is a valid Nylas status but won't occur locally because we delete
   * cancelled events.
   */
  public enum Status {
    CONFIRMED("confirmed"),
    TENTATIVE("tentative");

    @Getter
    public final String value;

    Status(String value) {
      this.value = value;
    }

    public static Status byStringValue(String value) {
      return Arrays.stream(Status.values())
          .filter(x -> x.getValue().equals(value))
          .findFirst()
          .orElseThrow(() -> {
            throw new IllegalArgumentException("Invalid event status: " + value);
          });
    }
  }
}
