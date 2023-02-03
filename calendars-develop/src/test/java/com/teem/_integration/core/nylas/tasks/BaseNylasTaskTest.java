package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import com.nylas.Event;
import com.nylas.NylasAccount;
import com.nylas.NylasApplication;
import com.UoU._fakes.EventPublisherMock;
import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU._integration.core.nylas.NylasTaskRunner;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.InternalCalendarsConfig;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventsConfig;
import com.UoU.core.nylas.mapping.NylasEventMapper;
import com.UoU.infra.jooq.tables.records.EventRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseNylasTaskTest extends BaseAppIntegrationTest {
  @Autowired
  @Getter
  TestDependencies dependencies;

  public NylasAccount getAccountClientMock() {
    return dependencies.getNylasAccountClientMock();
  }

  public NylasApplication getAppClientMock() {
    return dependencies.getNylasAppClientMock();
  }

  public NylasTaskRunner getNylasTaskRunnerSpy() {
    return dependencies.getNylasTaskRunnerSpy();
  }

  public NylasEventMapper getNylasEventMapperSpy() {
    return dependencies.getNylasEventMapperSpy();
  }

  public EventsConfig getEventsConfig() {
    return dependencies.getEventsConfig();
  }

  public EventPublisherMock getEventPublisherMock() {
    return dependencies.eventPublisherMock;
  }

  public EventPublisherMock.Verifier verifyEventPublisherMock() {
    return dependencies.eventPublisherMock.verify();
  }

  @AllArgsConstructor
  @Getter
  @Setter
  public static class TestDependencies {
    private NylasTaskRunner nylasTaskRunnerSpy;
    private NylasApplication nylasAppClientMock;
    private NylasAccount nylasAccountClientMock;
    private NylasEventMapper nylasEventMapperSpy;
    private EventsConfig eventsConfig;
    private EventPublisherMock eventPublisherMock;
    private InternalCalendarsConfig internalCalendarsConfig;
  }

  protected void validate(
      Event nylasEvent, EventId eventId, CalendarExternalId calendarExternalId) {
    validate(nylasEvent, dbHelper.getEvent(eventId), calendarExternalId);
  }

  protected void validate(
      Event nylasEvent, EventExternalId eventId, CalendarExternalId calendarExternalId) {
    validate(nylasEvent, dbHelper.getEventByExternalId(eventId), calendarExternalId);
  }

  private void validate(
      Event nylasEvent, EventRecord eventRecord, CalendarExternalId calendarExternalId) {

    assertThat(eventRecord.getId()).isNotNull();
    assertThat(eventRecord.getOrgId()).isNotNull();
    assertThat(eventRecord.getCreatedAt()).isNotNull();
    assertThat(nylasEvent.getIcalUid()).isEqualTo(eventRecord.getIcalUid());
    assertThat(nylasEvent.getCalendarId()).isEqualTo(calendarExternalId.value());
    assertThat(nylasEvent.getTitle()).isEqualTo(eventRecord.getTitle());
    assertThat(nylasEvent.getLocation()).isEqualTo(eventRecord.getLocation());
    assertThat(nylasEvent.getDescription()).isEqualTo(eventRecord.getDescription());

    val nylasWhen = (Event.Timespan) nylasEvent.getWhen();
    assertThat(nylasWhen.getStartTime()).isEqualTo(eventRecord.getStartAt().toInstant());
    assertThat(nylasWhen.getEndTime()).isEqualTo(eventRecord.getEndAt().toInstant());
    assertThat(eventRecord.getIsAllDay()).isFalse();

    val event =  dbHelper.getEventRepo().get(new EventId(eventRecord.getId()));
    assertThat(event.participants().size()).isGreaterThan(0);

    event.participants().forEach(p -> {
      assertThat(p.email()).isNotNull();
      assertThat(p.email()).isNotBlank();
      assertThat(p.status()).isNotNull();
    });
  }
}
