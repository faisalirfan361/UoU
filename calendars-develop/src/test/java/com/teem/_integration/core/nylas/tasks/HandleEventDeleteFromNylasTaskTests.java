package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.UoU._fakes.nylas.FakeInboundSyncLocker;
import com.UoU._helpers.TestData;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.Recurrence;
import com.UoU.core.nylas.ExternalEtag;
import lombok.val;
import org.jooq.exception.NoDataFoundException;
import org.junit.jupiter.api.Test;

public class HandleEventDeleteFromNylasTaskTests extends BaseNylasTaskTest {

  @Test
  void shouldAbortIfInboundSyncIsLockedForAccount() {
    val accountId = dbHelper.createAccount(orgId);
    val calendarId = dbHelper.createCalendar(orgId, accountId);
    val externalId = TestData.eventExternalId();
    dbHelper.createEvent(orgId, calendarId, externalId);

    FakeInboundSyncLocker.fakeIsAccountLockedResult(accountId, true);
    getNylasTaskRunnerSpy().handleEventDeleteFromNylas(accountId, externalId);

    assertThat(dbHelper.getEventRepo().tryGetByExternalId(externalId))
        .as("Task should have aborted, so event should still exist.")
        .isPresent();
  }

  @Test
  void shouldWork() {
    val accountId = dbHelper.createAccount(orgId);
    val calendarId = dbHelper.createCalendar(orgId, accountId);
    val externalId = TestData.eventExternalId();
    val eventId = dbHelper.createEvent(orgId, calendarId, externalId);

    // Create etag to make sure it gets deleted too:
    redisHelper.getExternalEtagRepo().save(externalId, new ExternalEtag("test"));

    getNylasTaskRunnerSpy().handleEventDeleteFromNylas(accountId, externalId);

    assertThrows(NoDataFoundException.class, () -> dbHelper.getEvent(eventId));
    assertThat(redisHelper.getExternalEtagRepo().get(externalId)).isEmpty();
    verifyEventPublisherMock()
        .noEventCreated()
        .noEventUpdated()
        .hasEventDeleted(DataSource.PROVIDER, eventId);
  }

  @Test
  void shouldWorkForRecurrenceMasterAndInstances() {
    val accountId = TestData.accountId(); // account not actually used currently
    val calendarId = dbHelper.createCalendar(orgId);
    val masterExternalId = TestData.eventExternalId();
    val masterId = dbHelper.createEvent(orgId, calendarId, x -> x
        .recurrence(TestData.recurrenceMaster())
        .externalId(masterExternalId));
    val instanceExternalId = TestData.eventExternalId();
    val instanceId = dbHelper.createEvent(orgId, calendarId, x -> x
        .recurrence(Recurrence.instance(masterId, false))
        .externalId(instanceExternalId));

    // Create etags to make sure they get deleted too:
    redisHelper.getExternalEtagRepo().save(masterExternalId, new ExternalEtag("master"));
    redisHelper.getExternalEtagRepo().save(instanceExternalId, new ExternalEtag("instance"));

    getNylasTaskRunnerSpy().handleEventDeleteFromNylas(accountId, masterExternalId);

    assertThrows(NoDataFoundException.class, () -> dbHelper.getEvent(masterId));
    assertThrows(NoDataFoundException.class, () -> dbHelper.getEvent(instanceId));
    assertThat(redisHelper.getExternalEtagRepo().get(masterExternalId)).isEmpty();
    assertThat(redisHelper.getExternalEtagRepo().get(instanceExternalId)).isEmpty();
    verifyEventPublisherMock()
        .noEventCreated()
        .noEventUpdated()
        .hasEventDeleted(DataSource.PROVIDER, masterId, instanceId);
  }

  @Test
  void shouldWorkWhenNotFoundLocally() {
    val accountId = TestData.accountId();
    val externalId = TestData.eventExternalId();

    assertThatCode(() -> getNylasTaskRunnerSpy().handleEventDeleteFromNylas(accountId, externalId))
        .doesNotThrowAnyException();

    verifyEventPublisherMock().noEventChangedOfAnyType();
  }
}
