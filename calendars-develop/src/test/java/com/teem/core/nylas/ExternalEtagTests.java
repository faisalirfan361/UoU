package com.UoU.core.nylas;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.nylas.Event;
import com.nylas.Participant;
import com.UoU._helpers.TestData;
import com.UoU.core.Fluent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ExternalEtagTests {

  @Test
  void ctor_shouldAllowNylasEvent() {
    val etag = new ExternalEtag(TestData.nylasEvent());
    assertThat(etag.toString()).isNotBlank();
  }

  @Test
  void ctor_shouldAllowEmptyNylasEvent() {
    val etag = new ExternalEtag(new Event());
    assertThat(etag.toString()).isNotBlank();
  }

  @Test
  void equals_shouldUseValue() {
    val etag1 = new ExternalEtag("value");
    val etag2 = new ExternalEtag("value");
    val etag3 = new ExternalEtag("value-different");

    assertThat(etag1.equals(etag2)).isTrue();
    assertThat(etag1.equals(etag3)).isFalse();
  }

  @ParameterizedTest
  @MethodSource
  void equals_shouldBeTrueWhenBasicFieldsAreEqual(Consumer<Event> changer) {
    val now = Instant.now();
    val events = Stream
        .generate(() -> Fluent
            .of(spy(new Event("calendar1", new Event.Timespan(now, now.plusSeconds(300)))))
            .also(x -> when(x.getId()).thenReturn("id"))
            .also(x -> when(x.getIcalUid()).thenReturn("icaluid"))
            .also(x -> x.setTitle("title"))
            .also(x -> x.setDescription("description"))
            .also(x -> x.setLocation("location"))
            .also(x -> x.setBusy(true))
            .also(x -> when(x.getReadOnly()).thenReturn(true))
            .also(x -> when(x.getOwner()).thenReturn("Owner <test@example.com>"))
            .also(x -> when(x.getMasterEventId()).thenReturn("abc123"))
            .also(x -> when(x.getOriginalStartTime()).thenReturn(now))
            .get())
        .limit(2)
        .toList();

    assertThat(new ExternalEtag(events.get(0)).equals(new ExternalEtag(events.get(1))))
        .as("Etags should be equal when nylas events are equal.")
        .isTrue();

    changer.accept(events.get(1));
    assertThat(new ExternalEtag(events.get(0)).equals(new ExternalEtag(events.get(1))))
        .as("Etags should not be equal when nylas events are not equal.")
        .isFalse();
  }

  static Stream<Consumer<Event>> equals_shouldBeTrueWhenBasicFieldsAreEqual() { // test data
    val str = TestData.uuidString();
    return Stream.of(
        x -> when(x.getId()).thenReturn(str),
        x -> when(x.getIcalUid()).thenReturn(str),
        x -> when(x.getCalendarId()).thenReturn(str),
        x -> x.setTitle(str),
        x -> x.setDescription(str),
        x -> x.setLocation(str),
        x -> x.setBusy(!x.getBusy()),
        x -> {
          val old = x.getReadOnly();
          when(x.getReadOnly()).thenReturn(!old);
        },
        x -> when(x.getOwner()).thenReturn(str),
        x -> when(x.getMasterEventId()).thenReturn(str),
        x -> {
          val old = x.getOriginalStartTime();
          when(x.getOriginalStartTime()).thenReturn(old.plusSeconds(1));
        });
  }

  @Test
  void equals_shouldBeTrueForEqualParticipantsInAnyOrder() {
    val etag1 = Fluent
        .of(new Event("calendar1", new Event.Date(LocalDate.MIN)))
        .also(x -> x.setParticipants(Stream
            .of(1, 2, 3)
            .map(i -> new Participant(i + "example.com").name("p" + i))
            .toList()))
        .map(ExternalEtag::new)
        .get();

    val etag2 = Fluent
        .of(new Event("calendar1", new Event.Date(LocalDate.MIN)))
        .also(x -> x.setParticipants(Stream
            .of(3, 1, 2) // out of order, should still be equal
            .map(i -> new Participant(i + "example.com").name("p" + i))
            .toList()))
        .map(ExternalEtag::new)
        .get();

    val etag3 = Fluent
        .of(new Event("calendar1", new Event.Date(LocalDate.MIN)))
        .also(x -> x.setParticipants(Stream
            .of(1, 2, 3)
            .map(i -> new Participant(i + "example.com").name("different" + i)) // different names
            .toList()))
        .map(ExternalEtag::new)
        .get();

    assertThat(etag1.equals(etag2)).isTrue();
    assertThat(etag1.equals(etag3)).isFalse();
  }

  @Test
  void equals_shouldBeTrueForEqualRecurrence() {
    val etag1 = Fluent
        .of(new Event("calendar1", new Event.Date(LocalDate.MIN)))
        .also(x -> x.setRecurrence(new Event.Recurrence("UTC", List.of("RRULE:DAILY"))))
        .map(ExternalEtag::new)
        .get();

    val etag2 = Fluent
        .of(new Event("calendar1", new Event.Date(LocalDate.MIN)))
        .also(x -> x.setRecurrence(new Event.Recurrence("UTC", List.of("RRULE:DAILY"))))
        .map(ExternalEtag::new)
        .get();

    val etag3 = Fluent
        .of(new Event("calendar1", new Event.Date(LocalDate.MIN)))
        .also(x -> x.setRecurrence(new Event.Recurrence("America/Denver", List.of("RRULE:DAILY"))))
        .map(ExternalEtag::new)
        .get();

    assertThat(etag1.equals(etag2)).isTrue();
    assertThat(etag1.equals(etag3)).isFalse();
  }

  @Test
  void equals_shouldBeTrueForEqualMetadataInAnyOrder() {
    val etag1 = Fluent
        .of(new Event("calendar1", new Event.Date(LocalDate.MIN)))
        .also(x -> x.setMetadata(Map.of("1", "a", "2", "b", "3", "c")))
        .map(ExternalEtag::new)
        .get();

    val etag2 = Fluent
        .of(new Event("calendar1", new Event.Date(LocalDate.MIN)))
        .also(x -> x.setMetadata(Map.of("3", "c", "1", "a", "2", "b"))) // out of order
        .map(ExternalEtag::new)
        .get();

    val etag3 = Fluent
        .of(new Event("calendar1", new Event.Date(LocalDate.MIN)))
        .also(x -> x.setMetadata(Map.of("1", "a", "2", "b", "3", "different"))) // diff
        .map(ExternalEtag::new)
        .get();

    assertThat(etag1.equals(etag2)).isTrue();
    assertThat(etag1.equals(etag3)).isFalse();
  }
}
