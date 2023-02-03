package com.UoU.core.nylas;

import static java.util.stream.Collectors.joining;

import com.nylas.Event;
import com.nylas.Participant;
import com.UoU.core.Checksum;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;

/**
 * Represents the Nylas state of an event to determine if an event has changed externally.
 *
 * <p>This is a checksum of all Nylas event fields that we sync, ignoring fields we don't sync.
 * Therefore, if an etag matches a previous etag for an event, the event hasn't changed.
 *
 * <p>The checksum value is NOT secure by design, so ensure that anyone/anything that can read the
 * checksum is able to read all the event data that goes into the checksum.
 */
@AllArgsConstructor
public class ExternalEtag {
  private static final String SEP = "|";

  private final String value;

  public ExternalEtag(Event event) {
    // Assume all fields could be null. Some should never be null, but etag should be permissive.
    this(new Checksum(
        event.getCalendarId(),
        event.getId(),
        event.getIcalUid(),
        event.getTitle(),
        event.getDescription(),
        event.getLocation(),
        Optional
            .ofNullable(event.getWhen())
            .map(x -> x.toString())
            .orElse(null),
        Optional
            .ofNullable(event.getRecurrence())
            // Ignore recurrence field for recurrence instances because we don't use or store it.
            // This field will be null for MS/Google instances anyway, but for virtual calendars,
            // Nylas provides the recurrence field on instances too. We only want it on masters.
            .filter(x -> event.getMasterEventId() == null)
            .map(x -> x.toString())
            .orElse(null),
        event.getMasterEventId(),
        Optional
            .ofNullable(event.getOriginalStartTime())
            .map(x -> x.toString())
            .orElse(null),
        event.getStatus(),
        Optional
            .ofNullable(event.getBusy())
            .map(x -> x.toString())
            .orElse(null),
        Optional
            .ofNullable(event.getReadOnly())
            .map(x -> x.toString())
            .orElse(null),
        event.getOwner(),
        Optional
            .ofNullable(event.getParticipants())
            .filter(x -> !x.isEmpty())
            .map(x -> x.stream()
                .sorted(Comparator.comparing(Participant::getEmail))
                .map(p -> p.getName() + SEP + p.getEmail() + SEP + p.getStatus() + SEP
                    + p.getComment())
                .collect(joining(SEP)))
            .orElse(null),
        Optional
            .ofNullable(event.getMetadata())
            .filter(x -> !x.isEmpty())
            .map(x -> x.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(joining(SEP)))
            .orElse(null)
    ).getValue());
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this || (obj instanceof ExternalEtag && value.equals(((ExternalEtag) obj).value));
  }
}
