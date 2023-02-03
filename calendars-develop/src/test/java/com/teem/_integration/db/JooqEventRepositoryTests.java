package com.UoU._integration.db;

import static com.UoU._helpers.PagingAssertions.assertPagesContainValues;
import static org.assertj.core.api.Assertions.assertThat;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU.core.Fluent;
import com.UoU.core.Noop;
import com.UoU.core.PageParams;
import com.UoU.core.events.Event;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventQuery;
import com.UoU.core.events.EventUpdateRequest;
import com.UoU.core.events.Owner;
import com.UoU.core.events.ParticipantRequest;
import com.UoU.core.events.ParticipantStatus;
import com.UoU.core.events.When;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.BooleanAssert;
import org.junit.jupiter.api.Test;

public class JooqEventRepositoryTests extends BaseAppIntegrationTest {

  @Test
  void list_shouldPageAndSortByStartTimeAndId() {
    val calendarId = dbHelper.createCalendar(orgId);
    val participants = dbHelper.createParticipantRequests().limit(2).toList();
    val now = Instant.now();

    // Create 4 events in order we expect them returned: by start time and then id.
    val ids = dbHelper.createEvents(
            orgId, calendarId,
            x -> x.when(createEventTimeSpan(now, Duration.ofSeconds(1)))
                .participants(participants),
            x -> x.when(createEventTimeSpan(now, Duration.ofSeconds(10)))
                .id(new EventId(UUID.fromString("aaaaaaaa-61e1-4411-bce5-cf0cbae5994b")))
                .participants(participants),
            x -> x.when(createEventTimeSpan(now, Duration.ofSeconds(10))) // same time as prev
                .id(new EventId(UUID.fromString("bbbbbbbb-61e1-4411-bce5-cf0cbae5994b")))
                .participants(participants),
            x -> x.when(createEventTimeSpan(now, Duration.ofSeconds(11)))
                .participants(participants))
        .toList();

    // Get the 4 items: 2 pages with 2 per page.
    val limit = 2;
    val repo = dbHelper.getEventRepo();
    val query = EventQuery.builder().orgId(orgId).calendarId(calendarId);
    val page1 = repo.list(query.page(new PageParams(null, limit)).build());
    val page2 = repo.list(query.page(new PageParams(page1.nextCursor(), limit)).build());

    assertPagesContainValues(
        x -> x.id(),
        Pair.of(page1, List.of(ids.get(0), ids.get(1))),
        Pair.of(page2, List.of(ids.get(2), ids.get(3))));
  }

  @Test
  void list_shouldFilterByWhen() {
    val calendarId = dbHelper.createCalendar(orgId);
    val id = dbHelper.createEvent(orgId, calendarId);
    val event = dbHelper.getEvent(id);
    val start = event.getStartAt().toInstant();
    val end = event.getEndAt().toInstant();

    final Function<Consumer<EventQuery.WhenQuery.Builder>, BooleanAssert> assertFound =
        whenCustomizer -> new BooleanAssert(Fluent
            .of(EventQuery.WhenQuery.builder())
            .also(whenCustomizer)
            .map(when -> EventQuery.builder()
                .orgId(orgId)
                .calendarId(calendarId)
                .when(when.build())
                .build())
            .map(query -> !dbHelper.getEventRepo().list(query).items().isEmpty())
            .get());

    assertFound.apply(x -> Noop.because("no filters"))
        .as("Should be found with no filters")
        .isTrue();

    assertFound.apply(x -> x
            .startsAfter(start.minusSeconds(1)).startsBefore(start.plusSeconds(1))
            .endsAfter(end.minusSeconds(1)).endsBefore(end.plusSeconds(1)))
        .as("Should be found with filters around start and end")
        .isTrue();

    assertFound.apply(x -> x.startsAfter(start))
        .as("Should NOT be found with startsAfter == event start")
        .isFalse();

    assertFound.apply(x -> x.startsBefore(start))
        .as("Should NOT be found with startsBefore == event start")
        .isFalse();

    assertFound.apply(x -> x.endsAfter(end))
        .as("Should NOT be found with endsAfter == event end")
        .isFalse();

    assertFound.apply(x -> x.endsBefore(end))
        .as("Should NOT be found with endsBefore == event end")
        .isFalse();
  }

  @Test
  void getAccountId_shouldReturnEmptyWhenNoAccount() {
    // Test is dumb-simple, but there was a null pointer bug because of a table join :(
    val id = dbHelper.createEvent(orgId, dbHelper.createCalendar(orgId));
    val result = dbHelper.getEventRepo().getAccountId(id);
    assertThat(result).isEmpty();
  }

  @Test
  void update_shouldOnlyUpdateSpecifiedFields() {
    val id = dbHelper.createEvent(
        orgId,
        dbHelper.createCalendar(orgId),
        x -> x.externalId(TestData.eventExternalId())
            .icalUid(TestData.uuidString())
            .status(Event.Status.CONFIRMED)
            .isReadOnly(true)
            .owner(new Owner("someone", TestData.email())));
    val updateRequest = EventUpdateRequest.builder()
        .id(id)
        .orgId(orgId)
        .title("new title") // update
        .description("new description") // update
        .build();

    val beforeRecord = dbHelper.getEvent(id);
    dbHelper.getEventRepo().update(updateRequest);
    val afterRecord = dbHelper.getEvent(id);

    // Ensure update worked:
    assertThat(afterRecord.getTitle()).isEqualTo(updateRequest.title());
    assertThat(afterRecord.getDescription()).isEqualTo(updateRequest.description());

    // Ensure unchanged fields kept old values:
    assertThat(afterRecord.getExternalId()).isEqualTo(beforeRecord.getExternalId());
    assertThat(afterRecord.getIcalUid()).isEqualTo(beforeRecord.getIcalUid());
    assertThat(afterRecord.getLocation()).isEqualTo(beforeRecord.getLocation());
    assertThat(afterRecord.getStatus()).isEqualTo(beforeRecord.getStatus());
    assertThat(afterRecord.getIsReadOnly()).isEqualTo(beforeRecord.getIsReadOnly());
    assertThat(afterRecord.getOwnerName()).isEqualTo(beforeRecord.getOwnerName());
    assertThat(afterRecord.getOwnerEmail()).isEqualTo(beforeRecord.getOwnerEmail());
  }

  /**
   * Participant status and comment are always null for API requests, since they are set by the
   * provider only, so ensure that updates preserve those fields.
   */
  @Test
  void update_shouldPreserveParticipantStatusAndCommentWhenNotChanged() {
    val calendarId = dbHelper.createCalendar(orgId);
    val participant = ParticipantRequest.builder()
        .name(TestData.uuidString())
        .email(TestData.email())
        .status(ParticipantStatus.MAYBE)
        .comment(TestData.uuidString())
        .build();
    val id = dbHelper.createEvent(
        orgId, calendarId, x -> x.participants(List.of(participant)));

    val updateRequest = EventUpdateRequest.builder()
        .id(id)
        .orgId(orgId)
        .participants(List.of(
            ParticipantRequest.builder()
                .email(participant.email())
                .name(participant.name() + " changed") // change name
                .build()))
        .build();

    dbHelper.getEventRepo().update(updateRequest);

    val resultParticipants = dbHelper.getEventRepo().get(id).participants();

    assertThat(resultParticipants).hasSize(1);
    assertThat(resultParticipants.get(0))
        .returns(updateRequest.participants().get(0).name(), x -> x.name())
        .returns(participant.email(), x -> x.email())
        .returns(participant.status(), x -> x.status())
        .returns(participant.comment(), x -> x.comment());
  }

  @Test
  void update_shouldInsertUpdateDeleteParticipants() {
    val calendarId = dbHelper.createCalendar(orgId);
    val participantToInsert = TestData.participantRequest();
    val participantToUpdate = TestData.participantRequest();
    val participantToDelete = TestData.participantRequest();
    val id = dbHelper.createEvent(
        orgId, calendarId, x -> x.participants(List.of(participantToUpdate, participantToDelete)));

    val updateName = participantToUpdate.name() + " changed";
    val updateRequest = EventUpdateRequest.builder()
        .id(id)
        .orgId(orgId)
        .participants(List.of(
            participantToInsert,
            ParticipantRequest.builder()
                .email(participantToUpdate.email())
                .name(updateName)
                .build()))
        .build();

    dbHelper.getEventRepo().update(updateRequest);

    val resultParticipants = dbHelper.getEventRepo().get(id).participants();
    val insertedParticipant = resultParticipants.stream()
        .filter(x -> x.email().equals(participantToInsert.email()))
        .findFirst();
    val updatedParticipant = resultParticipants.stream()
        .filter(x -> x.email().equals(participantToUpdate.email()))
        .findFirst();

    assertThat(resultParticipants).hasSize(2);
    assertThat(insertedParticipant).hasValueSatisfying(value -> assertThat(value.name())
        .isEqualTo(participantToInsert.name()));
    assertThat(updatedParticipant).hasValueSatisfying(value -> assertThat(value.name())
        .isEqualTo(updateName));
  }

  @Test
  void batchUpdate_shouldInsertUpdateDeleteParticipants() {
    val calendarId = dbHelper.createCalendar(orgId);
    val participantToInsert = TestData.participantRequest();
    val participantToUpdate = TestData.participantRequest();
    val participantToDelete = TestData.participantRequest();
    val id1 = dbHelper.createEvent(
        orgId, calendarId, x -> x.participants(List.of(participantToUpdate, participantToDelete)));
    val id2 = dbHelper.createEvent(
        orgId, calendarId, x -> x.participants(List.of(participantToUpdate, participantToDelete)));

    val updateName = participantToUpdate.name() + " changed";
    val updateRequest = EventUpdateRequest.builder()
        .orgId(orgId)
        .participants(List.of(
            participantToInsert,
            ParticipantRequest.builder()
                .email(participantToUpdate.email())
                .name(updateName)
                .build()));

    dbHelper.getEventRepo().batchUpdate(List.of(
        updateRequest.id(id1).build(),
        updateRequest.id(id2).build()));

    for (val id : List.of(id1, id2)) {
      val resultParticipants = dbHelper.getEventRepo().get(id).participants();
      val insertedParticipant = resultParticipants.stream()
          .filter(x -> x.email().equals(participantToInsert.email()))
          .findFirst();
      val updatedParticipant = resultParticipants.stream()
          .filter(x -> x.email().equals(participantToUpdate.email()))
          .findFirst();

      assertThat(resultParticipants).hasSize(2);
      assertThat(insertedParticipant).hasValueSatisfying(value -> assertThat(value.name())
          .isEqualTo(participantToInsert.name()));
      assertThat(updatedParticipant).hasValueSatisfying(value -> assertThat(value.name())
          .isEqualTo(updateName));
    }
  }

  /**
   * Participant status and comment are always null for API requests, since they are set by the
   * provider only, so ensure that updates preserve those fields when null.
   */
  @Test
  void batchUpdate_shouldPreserveParticipantStatusAndCommentWhenNotChanged() {
    val calendarId = dbHelper.createCalendar(orgId);
    val participant = ParticipantRequest.builder()
        .name(TestData.uuidString())
        .email(TestData.email())
        .status(ParticipantStatus.MAYBE)
        .comment(TestData.uuidString())
        .build();
    val id = dbHelper.createEvent(
        orgId, calendarId, x -> x.participants(List.of(participant)));

    val updateRequest = EventUpdateRequest.builder()
        .id(id)
        .orgId(orgId)
        .participants(List.of(
            ParticipantRequest.builder()
                .email(participant.email())
                .name(participant.name() + " changed") // change name
                .build()))
        .build();

    dbHelper.getEventRepo().batchUpdate(List.of(updateRequest));

    val resultParticipants = dbHelper.getEventRepo().get(id).participants();

    assertThat(resultParticipants).hasSize(1);
    assertThat(resultParticipants.get(0))
        .returns(updateRequest.participants().get(0).name(), x -> x.name())
        .returns(participant.email(), x -> x.email())
        .returns(participant.status(), x -> x.status())
        .returns(participant.comment(), x -> x.comment());
  }

  @Test
  void batchUpdate_shouldOnlyUpdateParticipantsWhenInUpdateFields() {
    val calendarId = dbHelper.createCalendar(orgId);
    val originalParticipants = TestData.participantRequestList(1);
    val id1 = dbHelper.createEvent(
        orgId, calendarId, x -> x.participants(originalParticipants));
    val id2 = dbHelper.createEvent(
        orgId, calendarId, x -> x.participants(originalParticipants));

    // Create 1 update request with title & participants changed, and other with only title changed.
    val changedTitle = TestData.uuidString();
    val changedParticipants = TestData.participantRequestList(2);
    val updateRequest1 = EventUpdateRequest.builder()
        .id(id1)
        .title(changedTitle)
        .participants(changedParticipants)
        .build();
    val updateRequest2 = new EventUpdateRequest(
        id2, null, null, orgId, changedTitle, null, null, null, null, null, true, true, null,
        changedParticipants, Set.of(EventUpdateRequest.UpdateField.TITLE), null);

    dbHelper.getEventRepo().batchUpdate(List.of(updateRequest1, updateRequest2));

    val result1 = dbHelper.getEventRepo().get(id1);
    val result2 = dbHelper.getEventRepo().get(id2);

    assertThat(result1.title())
        .as("Title 1 should have been updated because field was in updateFields")
        .isEqualTo(changedTitle);
    assertThat(result1.participants().stream().map(x -> x.email()))
        .as("Participants 1 should have been changed because field was in updateFields")
        .containsExactlyElementsOf(changedParticipants.stream().map(x -> x.email()).toList());

    assertThat(result2.title())
        .as("Title 2 should have been updated because field was in updateFields")
        .isEqualTo(changedTitle);
    assertThat(result2.participants().stream().map(x -> x.email()))
        .as("Participants 2 should NOT have been changed because field was NOT in updateFields")
        .containsExactlyElementsOf(originalParticipants.stream().map(x -> x.email()).toList());
  }

  private static When.TimeSpan createEventTimeSpan(Instant baseTime, Duration plusDuration) {
    return ModelBuilders.whenTimeSpan()
        .startTime(baseTime.plus(plusDuration))
        .endTime(baseTime.plus(plusDuration).plusSeconds(1))
        .build();
  }
}
