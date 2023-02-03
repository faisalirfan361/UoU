package com.UoU.core.nylas.tasks;

import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import java.util.Collection;
import java.util.UUID;

/**
 * Schedules Nylas tasks to run asynchronously.
 *
 * <p>Sync operations are inbound (imports), outbound (exports), or both (two-way sync). Most of the
 * methods and docs below use this terminology to clarify which direction data flows, although some
 * operations don't fit neatly into this convention.
 */
public interface NylasTaskScheduler {

  /**
   * Batch version of {@link #updateSubaccountToken(ServiceAccountId, AccountId)}.
   */
  void updateAllSubaccountTokens(ServiceAccountId serviceAccountId);

  /**
   * Updates a subaccount token by fetching from Nylas and storing locally.
   */
  void updateSubaccountToken(ServiceAccountId serviceAccountId, AccountId accountId);

  /**
   * Inbound: Imports the latest account sync state from Nylas and stores locally.
   */
  void updateAccountSyncState(AccountId accountId);

  /**
   * Outbound: Deletes account from Nylas after it's already been deleted locally.
   */
  void deleteAccountFromNylas(AccountId accountId);

  /**
   * Inbound: Batch version of
   * {@link #importCalendarFromNylas(AccountId, CalendarExternalId, boolean)}.
   */
  void importAllCalendarsFromNylas(AccountId accountId, boolean includeEvents);

  /**
   * Inbound: Imports calendar info from Nylas, and optionally triggers a sync of events as well.
   */
  void importCalendarFromNylas(
      AccountId accountId, CalendarExternalId calendarId, boolean includeEvents);

  /**
   * Inbound: Deletes local calendar in response to Nylas deleting the calendar.
   */
  void handleCalendarDeleteFromNylas(AccountId accountId, CalendarExternalId calendarId);

  /**
   * Outbound: Exports new calendar or calendar changes to Nylas (internal calendars only).
   *
   * <p>Currently, this only supports internal calendars.
   */
  void exportCalendarsToNylas(Collection<CalendarId> ids, boolean includeEvents);

  /**
   * Inbound/Outbound: Does a 2-way sync of all events on the calendar.
   */
  default void syncAllEvents(AccountId accountId, CalendarId calendarId) {
    syncAllEvents(accountId, calendarId, false);
  }


  /**
   * Inbound/Outbound: Does a 2-way sync of all events on the calendar.
   *
   * @param accountId                   The account id of the calendar.
   * @param calendarId                  The calendar id.
   * @param forceUpdateAllDayEventWhens Updates every all-day event's when, which can be useful when
   *                                    the calendar timezone changes so that calculated all-day
   *                                    timestamps will change for the new timezone.
   */
  default void syncAllEvents(
      AccountId accountId, CalendarId calendarId, boolean forceUpdateAllDayEventWhens) {
    syncAllEvents(accountId, calendarId, forceUpdateAllDayEventWhens, null);
  }

  /**
   * Inbound/Outbound: Does a 2-way sync of all events on the calendar, with full options.
   *
   * @param accountId                   The account id of the calendar.
   * @param calendarId                  The calendar id.
   * @param inboundSyncAccountLock      Indicates that this operation is part of a parent operation
   *                                    that has already locked the account. The task will decrement
   *                                    the lock so that the account will become unlocked once all
   *                                    child operations are complete.
   * @param forceUpdateAllDayEventWhens Updates every all-day event's when, which can be useful when
   *                                    the calendar timezone changes so that calculated all-day
   *                                    timestamps will change for the new timezone.
   */
  void syncAllEvents(
      AccountId accountId, CalendarId calendarId, boolean forceUpdateAllDayEventWhens,
      UUID inboundSyncAccountLock);

  /**
   * Inbound: Imports an event from Nylas to local.
   */
  void importEventFromNylas(AccountId accountId, EventExternalId externalId);

  /**
   * Inbound: Deletes local event in response to Nylas deleting the event.
   */
  void handleEventDeleteFromNylas(AccountId accountId, EventExternalId externalId);

  /**
   * Outbound: Exports local event to Nylas.
   */
  void exportEventToNylas(AccountId accountId, EventId eventId);

  /**
   * Outbound: Deletes event from Nylas after it's already been deleted locally.
   */
  void deleteEventFromNylas(AccountId accountId, EventExternalId externalId);
}
