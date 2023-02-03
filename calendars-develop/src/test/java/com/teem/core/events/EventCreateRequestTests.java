package com.UoU.core.events;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU._helpers.ModelBuilders;
import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.conferencing.ConferencingConstraints;
import com.UoU.core.conferencing.ConferencingMeetingCreateRequest;
import com.UoU.core.conferencing.ConferencingUserId;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EventCreateRequestTests {

  @Test
  void validation_shouldPass() {
    assertThatValidationPasses(buildValid().build());
    assertThatValidationPasses(buildValid().owner(TestData.owner()).build());
    assertThatValidationPasses(
        buildValid().participants(TestData.participantRequestList(2)).build());
    assertThatValidationPasses(
        buildValid().recurrence(Recurrence.instance(EventId.create(), false)).build());
    assertThatValidationPasses(buildValid()
        .title("x".repeat(EventConstraints.TITLE_MAX))
        .description("x".repeat(EventConstraints.DESCRIPTION_MAX))
        .location("x".repeat(EventConstraints.LOCATION_MAX))
        .conferencing(new ConferencingMeetingCreateRequest(
            TestData.email(),
            ConferencingUserId.create(),
            "x".repeat(ConferencingConstraints.LANGUAGE_MAX)))
        .dataSource(DataSource.fromApi("x".repeat(EventConstraints.DATA_SOURCE_API_MAX)))
        .build());
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, EventCreateRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("id", "orgId", "calendarId", "when"),
            EventCreateRequest.builder().build()), // everything is null
        Arguments.of(
            Set.of("orgId.value"),
            buildValid().orgId(new OrgId(" ")).build()),
        Arguments.of(
            Set.of("calendarId.value"),
            buildValid().calendarId(new CalendarId(" ")).build()),
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
            Set.of("when.startDate", "when.endDate"),
            buildValid().when(new When.DateSpan(null, null)).build()),
        Arguments.of(
            Set.of("when.startTime", "when.endTime"),
            buildValid().when(new When.TimeSpan(null, null)).build()),
        Arguments.of(
            Set.of("recurrence.master.rrule", "recurrence.master.timezone"),
            buildValid().recurrence(Recurrence.master(List.of(), " ")).build()),
        Arguments.of(
            Set.of("owner.email"),
            buildValid().owner(new Owner("someone", "not-an-email")).build()),
        Arguments.of(
            Set.of("conferencing.principalEmail", "conferencing.userId"),
            buildValid()
                .conferencing(new ConferencingMeetingCreateRequest(null, null, null))
                .build()),
        Arguments.of(
            Set.of("conferencing.principalEmail"),
            buildValid()
                .conferencing(new ConferencingMeetingCreateRequest(
                    "not-an-email", ConferencingUserId.create(), "en"))
                .build()),
        Arguments.of(
            Set.of("conferencing.language"),
            buildValid()
                .conferencing(new ConferencingMeetingCreateRequest(
                    "x@y.co",
                    ConferencingUserId.create(),
                    "x".repeat(ConferencingConstraints.LANGUAGE_MAX + 1)))
                .build()),
        Arguments.of(
            Set.of("participants[0].email"),
            buildValid().participants(List.of(
                ParticipantRequest.builder().status(ParticipantStatus.MAYBE).email("x").build()
            )).build())
    );
  }

  private static EventCreateRequest.Builder buildValid() {
    return ModelBuilders.eventCreateRequestWithTestData();
  }
}
