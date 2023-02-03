package com.UoU.core.events;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;
import static com.UoU.core.events.EventUpdateRequest.UpdateField;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class EventUpdateRequestTests {

  @Test
  void validation_shouldPass() {
    assertThatValidationPasses(buildValid().build());
    assertThatValidationPasses(buildValid().owner(TestData.owner()).build());
    assertThatValidationPasses(
        buildValid().participants(TestData.participantRequestList(1)).build());
    assertThatValidationPasses(
        buildValid().recurrence(TestData.recurrenceMaster().getMaster()).build());
    assertThatValidationPasses(buildValid()
        .title("x".repeat(EventConstraints.TITLE_MAX))
        .description("x".repeat(EventConstraints.DESCRIPTION_MAX))
        .location("x".repeat(EventConstraints.LOCATION_MAX))
        .dataSource(DataSource.fromApi("x".repeat(EventConstraints.DATA_SOURCE_API_MAX)))
        .build());
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, EventUpdateRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("id", "orgId", "when"),
            new EventUpdateRequest(null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null)), // everything is null
        Arguments.of(
            Set.of("orgId.value"),
            buildValid().orgId(new OrgId(" ")).build()),
        Arguments.of(
            Set.of("externalId.value"),
            buildValid().externalId(new EventExternalId(" ")).build()),
        Arguments.of(
            Set.of("title"),
            buildValid().title("x".repeat(EventConstraints.TITLE_MAX + 1)).build()),
        Arguments.of(
            Set.of("description"),
            buildValid().description("x".repeat(EventConstraints.DESCRIPTION_MAX + 1)).build()),
        Arguments.of(
            Set.of("location"),
            buildValid().location("x".repeat(EventConstraints.LOCATION_MAX + 1)).build()),
        Arguments.of(
            Set.of("dataSource.value"),
            buildValid()
                .dataSource(
                    DataSource.fromApi("x".repeat(EventConstraints.DATA_SOURCE_API_MAX + 1)))
                .build()),
        Arguments.of(
            Set.of("when.date"),
            buildValid().when(new When.Date(null)).build()),
        Arguments.of(
            Set.of("recurrence.rrule", "recurrence.timezone"),
            buildValid().recurrence(Recurrence.master(List.of(), "").getMaster()).build()),
        Arguments.of(
            Set.of("owner.email"),
            buildValid().owner(new Owner("someone", "not-an-email")).build()
        ),
        Arguments.of(
            Set.of("participants[0].email"),
            buildValid().participants(List.of(
                ParticipantRequest.builder().status(ParticipantStatus.NO).email("").build()
            )).build())
    );
  }

  @Test
  void updateFields_shouldBeUnmodifiable() {
    val updateFields = new HashSet<UpdateField>();
    updateFields.add(UpdateField.STATUS);

    val request = new EventUpdateRequest(
        null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        updateFields, null);

    assertThatCode(() -> request.updateFields().add(UpdateField.TITLE))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatCode(() -> request.updateFields().remove(UpdateField.STATUS))
        .isInstanceOf(UnsupportedOperationException.class);

    assertThat(request.updateFields()).containsExactly(UpdateField.STATUS);
  }

  @Test
  void hasUpdates_shouldBeTrueWhenUpdateFieldsHasItems() {
    assertThat(builder().title(null).build().hasUpdates()).isTrue();
    assertThat(builder().build().hasUpdates()).isFalse();
  }

  @Test
  void hasUpdates_shouldBeFalseWhenOnlyDataSourceHasChanged() {
    assertThat(builder().dataSource(DataSource.fromApi("test")).build().hasUpdates()).isFalse();
  }

  @Test
  void hasUpdate_shouldBeTrueWhenUpdateFieldsHasSpecifiedField() {
    val field = UpdateField.LOCATION;
    assertThat(builder().location(null).build().hasUpdate(field)).isTrue();
    assertThat(builder().title(null).build().hasUpdate(field)).isFalse();
  }

  @Test
  void withMatchingUpdateFieldsRemoved_shouldWork() {
    val event = ModelBuilders.eventWithTestData().build();
    val instance = EventUpdateRequest.builder()
        .id(event.id())
        .title(TestData.uuidString())
        .description(event.description())
        .build();

    val result = instance.withMatchingUpdateFieldsRemoved(event);

    // removeMatchingUpdateFields is tested separately, so we only need some basic checks.
    assertThat(result.updateFields())
        .contains(UpdateField.TITLE)
        .doesNotContain(UpdateField.DESCRIPTION);
  }

  @Test
  void toBuilder_shouldResultInEqualInstanceWhenNothingChanges() {
    val builder = ModelBuilders
        .eventUpdateRequestWithTestData()
        .title("test");
    val instance = builder.build();

    assertThat(instance.toBuilder().build())
        .as("toBuilder instance should equal original instance when nothing changed.")
        .isEqualTo(instance);

    assertThat(instance.toBuilder().title(null).build())
        .as("toBuilder instance should NOT equal original instance when something changed.")
        .isNotEqualTo(instance);
  }

  private static EventUpdateRequest.Builder builder() {
    return EventUpdateRequest.builder().id(EventId.create());
  }

  private static EventUpdateRequest.Builder buildValid() {
    return ModelBuilders.eventUpdateRequestWithTestData();
  }

  static class BuilderTests {

    @Test
    void build_shouldRequireId() {
      val builder = EventUpdateRequest.builder();

      assertThatCode(builder::build)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("id");
    }

    @ParameterizedTest
    @EnumSource(UpdateField.class)
    void build_shouldAddSetFieldToUpdateFields(UpdateField field) {
      val builder = EventUpdateRequest.builder().id(EventId.create());

      assertThat(builder.build().updateFields())
          .as("Builder should have no updateFields to start.")
          .isEmpty();

      // Apply setter for single field.
      switch (field) {
        case EXTERNAL_ID -> builder.externalId(null);
        case ICAL_UID -> builder.icalUid(null);
        case TITLE -> builder.title(null);
        case DESCRIPTION -> builder.description(null);
        case LOCATION -> builder.location(null);
        case WHEN -> builder.when(null);
        case RECURRENCE -> builder.recurrence(null);
        case STATUS -> builder.status(null);
        case IS_BUSY -> builder.isBusy(false);
        case IS_READ_ONLY -> builder.isReadOnly(false);
        case OWNER -> builder.owner(null);
        case PARTICIPANTS -> builder.participants(null);
        default -> throw new IllegalArgumentException("Unhandled enum value");
      }

      assertThat(builder.build().updateFields())
          .as("Single set field should be in updateFields.")
          .containsExactly(field);
    }

    @Test
    void removeUpdateFields_shouldRemoveOnlySpecifiedFields() {
      val builder = EventUpdateRequest.builder()
          .id(EventId.create())
          .title("test")
          .description("test")
          .status(Event.Status.CONFIRMED);

      val before = builder.build().updateFields();
      val after = builder
          .removeUpdateFields(UpdateField.DESCRIPTION, UpdateField.STATUS)
          .build()
          .updateFields();

      assertThat(before).containsExactlyInAnyOrder(
          UpdateField.TITLE, UpdateField.DESCRIPTION, UpdateField.STATUS);
      assertThat(after).containsExactlyInAnyOrder(
          UpdateField.TITLE);
    }

    @ParameterizedTest
    @EnumSource(UpdateField.class)
    void removeMatchingUpdateFields_shouldKeepChangedField(UpdateField field) {
      val when = TestData.whenTimeSpan();
      val event = ModelBuilders.eventWithTestData()
          .when(when)
          .recurrence(TestData.recurrenceMaster())
          .status(Event.Status.CONFIRMED)
          .owner(TestData.owner())
          .participants(List.of(
              new Participant("a", "a@example.com", null, "z"),
              new Participant("b", "b@example.com", null, "x")))
          .build();

      // Create builder that uses values matching the event but in copied objects so we can test
      // that equality is based on values and not object references.
      Supplier<EventUpdateRequest.Builder> builder = () -> EventUpdateRequest.builder()
          .id(event.id())
          .icalUid(event.icalUid())
          .externalId(new EventExternalId(event.externalId().value()))
          .title(event.title())
          .description(event.description())
          .location(event.location())
          .when(new When.TimeSpan(
              Instant.ofEpochMilli(when.startTime().toEpochMilli()),
              Instant.ofEpochMilli(when.endTime().toEpochMilli())))
          .recurrence(event.recurrence()
              .withMaster()
              .map(x -> new Recurrence.Master(x.rrule(), x.timezone()))
              .orElseThrow())
          .status(event.status())
          .isBusy(event.isBusy())
          .isReadOnly(event.isReadOnly())
          .owner(Fluent.of(event.owner()).map(x -> new Owner(x.name(), x.email())).get())
          .participants(event.participants().stream()
              .map(x -> ParticipantRequest.builder()
                  .name(x.name())
                  .email(x.email())
                  .status(x.status())
                  .comment(x.comment())
                  .build())
              .sorted(Comparator.comparing(x -> x.comment())) // sort different than event
              .toList());

      val builder1 = builder.get();
      builder1.removeMatchingUpdateFields(event);
      assertThat(builder1.build().updateFields())
          .as("Update fields should be empty before changing the builder value")
          .isEmpty();

      // Create another builder with single changed field:
      val builder2 = builder.get();
      switch (field) {
        case EXTERNAL_ID -> builder2.externalId(TestData.eventExternalId());
        case ICAL_UID -> builder2.icalUid(TestData.uuidString());
        case TITLE -> builder2.title(TestData.uuidString());
        case DESCRIPTION -> builder2.description(TestData.uuidString());
        case LOCATION -> builder2.location(TestData.uuidString());
        case WHEN -> builder2.when(TestData.whenTimeSpan());
        case RECURRENCE -> builder2.recurrence(TestData.recurrenceMaster().getMaster());
        case STATUS -> builder2.status(Event.Status.TENTATIVE);
        case IS_BUSY -> builder2.isBusy(!builder2.getIsBusy());
        case IS_READ_ONLY -> builder2.isReadOnly(!builder2.getIsReadOnly());
        case OWNER -> builder2.owner(TestData.owner());
        case PARTICIPANTS -> builder2.participants(TestData.participantRequestList(2));
        default -> throw new IllegalArgumentException("Unhandled enum value");
      }

      builder2.removeMatchingUpdateFields(event);
      assertThat(builder2.build().updateFields())
          .as(field + " should be the only update field after changing the builder value")
          .containsExactly(field);
    }

    @Test
    void removeMatchingUpdateFields_shouldHandleNewAndExistingParticipants() {
      val event = ModelBuilders.eventWithTestData()
          .participants(List.of(
              new Participant("a", "a@x.y", ParticipantStatus.MAYBE, "a"),
              new Participant("b", "b@x.y", ParticipantStatus.YES, "b"),
              new Participant("c", "c@x.y", ParticipantStatus.NO, "c")))
          .build();

      val builder = EventUpdateRequest.builder()
          .id(event.id())
          .participants(List.of(
              ParticipantRequest.builder().name("a").email("a@x.y").build(), // unchanged
              ParticipantRequest.builder().name("b").email("b@x.y")
                  .status(ParticipantStatus.NO).build(), // status changed
              ParticipantRequest.builder().name("d").email("d@x.y").build() // new
          ));

      builder.removeMatchingUpdateFields(event);
      val built = builder.build();

      assertThat(built.participants().stream().map(x -> x.name())).containsExactly("a", "b", "d");
      assertThat(built.updateFields()).contains(UpdateField.PARTICIPANTS);
      assertThat(built.participants().get(0).hasUpdates()).isFalse();
      assertThat(built.participants().get(1).hasUpdates()).isTrue();
      assertThat(built.participants().get(1).updateFields())
          .containsExactly(ParticipantRequest.UpdateField.STATUS);
      assertThat(built.participants().get(2).hasUpdates()).isTrue();
    }

    @Test
    void removeMatchingUpdateFields_shouldMatchWhenDateWithAndWithoutEffectiveUtcTimeSpan() {
      val date = LocalDate.now();
      val event = ModelBuilders.eventWithTestData()
          .when(new When.Date(date, TestData.timeSpan()))
          .build();

      val builder = EventUpdateRequest.builder()
          .id(event.id())
          .when(new When.Date(date));

      builder.removeMatchingUpdateFields(event);
      val built = builder.build();

      assertThat(built.hasUpdates()).isFalse();
    }

    @Test
    void removeMatchingUpdateFields_shouldMatchWhenDateSpanWithAndWithoutEffectiveUtcTimeSpan() {
      val date = LocalDate.now();
      val event = ModelBuilders.eventWithTestData()
          .when(new When.DateSpan(date, date.plusDays(1), TestData.timeSpan()))
          .build();

      val builder = EventUpdateRequest.builder()
          .id(event.id())
          .when(new When.DateSpan(date, date.plusDays(1)));

      builder.removeMatchingUpdateFields(event);
      val built = builder.build();

      assertThat(built.hasUpdates()).isFalse();
    }

    @Test
    void removeMatchingUpdateFields_shouldNotMatchWhenDateWithDifferentEffectiveUtcTimeSpans() {
      val date = LocalDate.now();
      val event = ModelBuilders.eventWithTestData()
          .when(new When.Date(date, TestData.timeSpan()))
          .build();

      val builder = EventUpdateRequest.builder()
          .id(event.id())
          .when(new When.Date(date, TestData.timeSpan()));

      builder.removeMatchingUpdateFields(event);
      val built = builder.build();

      assertThat(built.hasUpdate(UpdateField.WHEN)).isTrue();
    }

    @Test
    void removeMatchingUpdateFields_shouldNotUpdateRecurrenceWhenOnlyValidationContextDiffers() {
      val event = ModelBuilders.eventWithTestData()
          .recurrence(Recurrence.master(
              List.of("RRULE:FREQ=DAILY"), "UTC"))
          .build();

      val builder = EventUpdateRequest.builder()
          .id(event.id())
          .recurrence(new Recurrence.Master(
              List.of("RRULE:FREQ=DAILY"),
              "UTC",
              new Recurrence.Master.ValidationContext(true)));

      builder.removeMatchingUpdateFields(event);
      val built = builder.build();

      assertThat(built.hasUpdate(UpdateField.RECURRENCE)).isFalse();
    }

    @Test
    void removeMatchingUpdateFields_shouldNotUpdateRecurrenceWhenNullAndNone() {
      val event = ModelBuilders.eventWithTestData()
          .recurrence(Recurrence.none())
          .build();

      val builder = EventUpdateRequest.builder()
          .id(event.id())
          .recurrence(null);

      builder.removeMatchingUpdateFields(event);
      val built = builder.build();

      assertThat(built.hasUpdate(UpdateField.RECURRENCE)).isFalse();
    }

    @Test
    void removeMatchingUpdateFields_shouldNotUpdateRecurrenceForInstanceAndMaster() {
      val event = ModelBuilders.eventWithTestData()
          .recurrence(TestData.recurrenceInstance())
          .build();

      val builder = EventUpdateRequest.builder()
          .id(event.id())
          .recurrence(TestData.recurrenceInstance().getMaster());

      builder.removeMatchingUpdateFields(event);
      val built = builder.build();

      assertThat(built.hasUpdate(UpdateField.RECURRENCE)).isFalse();
    }

    @Test
    void removeMatchingUpdateFields_shouldUpdateRecurrenceWithDifferentTimezone() {
      val rrule = List.of("RRULE:FREQ=DAILY");
      val event = ModelBuilders.eventWithTestData()
          .recurrence(Recurrence.master(rrule, "UTC"))
          .build();

      val builder = EventUpdateRequest.builder()
          .id(event.id())
          .recurrence(new Recurrence.Master(rrule, "America/Denver"));

      builder.removeMatchingUpdateFields(event);
      val built = builder.build();

      assertThat(built.hasUpdate(UpdateField.RECURRENCE)).isTrue();
    }
  }
}
