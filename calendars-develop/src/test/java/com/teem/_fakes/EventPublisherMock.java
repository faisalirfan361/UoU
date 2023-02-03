package com.UoU._fakes;

import static org.assertj.core.api.Assertions.assertThat;

import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventPublisher;
import java.util.ArrayList;
import java.util.Collection;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Simple mock event publisher because Mockito was more trouble than it was worth for this case.
 */
public class EventPublisherMock implements EventPublisher {
  private final ArrayList<EventId> created = new ArrayList<>();
  private final ArrayList<EventId> updated = new ArrayList<>();
  private final ArrayList<Pair<EventId, DataSource>> deleted = new ArrayList<>();

  @Override
  public void eventCreated(Collection<EventId> eventIds) {
    created.addAll(eventIds);
  }

  @Override
  public void eventUpdated(Collection<EventId> eventIds) {
    updated.addAll(eventIds);
  }

  @Override
  public void eventDeleted(
      OrgId orgId, CalendarId calendarId, Collection<EventId> eventIds, DataSource dataSource) {
    deleted.addAll(eventIds.stream().map(x -> Pair.of(x, dataSource)).toList());
  }

  public void reset() {
    created.clear();
    updated.clear();
    deleted.clear();
  }

  public Verifier verify() {
    return new Verifier();
  }

  @AllArgsConstructor
  public class Verifier {

    public Verifier hasEventCreated(EventId... expectedIds) {
      assertThat(created)
          .as("eventCreated should have published ids")
          .containsExactlyInAnyOrder(expectedIds);
      return this;
    }

    public Verifier noEventCreated() {
      assertThat(created)
          .as("eventCreated should have NO published ids")
          .isEmpty();
      return this;
    }

    public Verifier hasEventUpdated(EventId... expectedIds) {
      assertThat(updated)
          .as("eventUpdated should have published ids")
          .containsExactlyInAnyOrder(expectedIds);
      return this;
    }

    public Verifier noEventUpdated() {
      assertThat(updated)
          .as("eventUpdated should have NO published ids")
          .isEmpty();
      return this;
    }

    public Verifier hasEventDeleted(DataSource dataSource, EventId... expectedIds) {
      assertThat(deleted.stream().map(x -> x.getLeft()))
          .as("eventDeleted should have published ids")
          .containsExactlyInAnyOrder(expectedIds);
      assertThat(deleted.stream().map(x -> x.getRight()))
          .as("eventDeleted ids should all have dataSource: " + dataSource.value())
          .allMatch(dataSource::equals);
      return this;
    }

    public Verifier noEventDeleted() {
      assertThat(deleted)
          .as("eventDeleted should have NO published ids")
          .isEmpty();
      return this;
    }

    public Verifier noEventChangedOfAnyType() {
      noEventCreated();
      noEventUpdated();
      noEventDeleted();
      return this;
    }

    public Verifier resetMock() {
      reset();
      return this;
    }
  }
}
