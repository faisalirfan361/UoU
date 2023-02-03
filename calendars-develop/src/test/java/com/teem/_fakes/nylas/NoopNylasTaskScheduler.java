package com.UoU._fakes.nylas;

import com.UoU.core.Noop;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import java.util.Collection;
import java.util.UUID;

/**
 * Nylas scheduler that does nothing for testing.
 */
public class NoopNylasTaskScheduler implements NylasTaskScheduler {

  @Override
  public void updateAllSubaccountTokens(ServiceAccountId serviceAccountId) {
    Noop.because("testing");
  }

  @Override
  public void updateSubaccountToken(ServiceAccountId serviceAccountId, AccountId accountId) {
    Noop.because("testing");
  }

  @Override
  public void updateAccountSyncState(AccountId accountId) {
    Noop.because("testing");
  }

  @Override
  public void deleteAccountFromNylas(AccountId accountId) {
    Noop.because("testing");
  }

  @Override
  public void importAllCalendarsFromNylas(AccountId accountId, boolean includeEvents) {
    Noop.because("testing");
  }

  @Override
  public void importCalendarFromNylas(
      AccountId accountId, CalendarExternalId calendarExternalId, boolean includeEvents) {
    Noop.because("testing");
  }

  @Override
  public void handleCalendarDeleteFromNylas(
      AccountId accountId, CalendarExternalId calendarExternalId) {
    Noop.because("testing");
  }

  @Override
  public void exportCalendarsToNylas(Collection<CalendarId> ids, boolean includeEvents) {
    Noop.because("testing");
  }

  @Override
  public void syncAllEvents(
      AccountId accountId, CalendarId calendarId, boolean forceUpdateAllDayEventWhens,
      UUID inboundSyncAccountLock) {
    Noop.because("testing");
  }

  @Override
  public void importEventFromNylas(AccountId accountId, EventExternalId externalId) {
    Noop.because("testing");
  }

  @Override
  public void handleEventDeleteFromNylas(AccountId accountId, EventExternalId externalId) {
    Noop.because("testing");
  }

  @Override
  public void exportEventToNylas(AccountId accountId, EventId eventId) {
    Noop.because("testing");
  }

  @Override
  public void deleteEventFromNylas(AccountId accountId, EventExternalId externalId) {
    Noop.because("testing");
  }
}
