package com.UoU.infra.kafka.mapping;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.EventId;
import com.UoU.core.mapping.WrappedValueMapperImpl;
import com.UoU.infra.avro.publicevents.EventChangeType;
import lombok.val;
import org.junit.jupiter.api.Test;

/**
 * Sanity-check tests for public event mappings because it's really bad if these break.
 */
class PublicEventMapperTests {
  private static final PublicEventMapper MAPPER = new PublicEventMapperImpl(
      new WrappedValueMapperImpl());

  @Test
  void toEventChangedAvro_created_shouldWork() {
    val participants = TestData.participantList(2);
    val event = ModelBuilders.eventWithTestData().participants(participants).build();
    val result = MAPPER.toEventChangedAvro(
        event, EventChangeType.created, DataSource.PROVIDER);

    assertThat(result.getChangeType()).isEqualTo(EventChangeType.created);
    assertThat(result.getChangeSource()).hasValue(DataSource.PROVIDER.value());
    assertThat(result.getEventId()).isEqualTo(event.id().value().toString());
    assertThat(result.getEvent()).isPresent();
    assertThat(result.getEvent().orElseThrow().getParticipants())
        .hasSize(participants.size())
        .extracting(x -> x.getEmail())
        .containsExactlyInAnyOrder(
            participants.stream().map(x -> x.email()).toArray(String[]::new));
  }

  @Test
  void toEventChangedAvro_updated_shouldWork() {
    val event = TestData.event();
    val result = MAPPER.toEventChangedAvro(
        event, EventChangeType.updated, DataSource.PROVIDER);

    assertThat(result.getChangeType()).isEqualTo(EventChangeType.updated);
    assertThat(result.getChangeSource()).hasValue(DataSource.PROVIDER.value());
    assertThat(result.getEventId()).isEqualTo(event.id().value().toString());
    assertThat(result.getEvent()).isPresent();
  }

  @Test
  void toEventChangedDeletedAvro_shouldWork() {
    val eventId = EventId.create();
    val result = MAPPER.toEventChangedDeletedAvro(
        TestData.orgId(), CalendarId.create(), eventId, DataSource.PROVIDER);

    assertThat(result.getChangeType()).isEqualTo(EventChangeType.deleted);
    assertThat(result.getEventId()).isEqualTo(eventId.value().toString());
    assertThat(result.getEvent()).isEmpty();
  }
}
