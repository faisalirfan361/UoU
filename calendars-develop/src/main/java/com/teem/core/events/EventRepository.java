package com.UoU.core.events;

import com.UoU.core.OrgId;
import com.UoU.core.PagedItems;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarId;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public interface EventRepository {
  PagedItems<Event> list(EventQuery query);

  Stream<Event> listById(Collection<EventId> ids);

  // DO-LATER: Remove this non-paged method when nylas sync doesn't rely on it and batches better.
  Stream<Event> listByCalendar(OrgId orgId, CalendarId calendarId);

  Stream<Pair<EventId, Optional<EventExternalId>>> listRecurrenceInstanceIdPairs(
      EventId masterEventId);

  Stream<Event> listRecurrenceInstances(EventId masterEventId);

  Event get(EventId id);

  Optional<Event> tryGet(EventId id);

  Optional<Event> tryGetByExternalId(EventExternalId externalId);

  EventAccessInfo getAccessInfo(EventId id);

  Recurrence getRecurrence(EventId id);

  CoreIds getCoreIds(EventId id);

  CoreIds getCoreIds(EventExternalId id);

  Optional<EventId> getId(EventExternalId externalId);

  Optional<EventExternalId> getExternalId(EventId id);

  Pair<Optional<AccountId>, Optional<EventExternalId>> getAccountAndExternalIds(EventId id);

  Optional<String> getIcalUid(EventId id);

  void create(EventCreateRequest request);

  void update(EventUpdateRequest request);

  void updateExternalId(EventId id, EventExternalId externalId, DataSource dataSource);

  void updateRecurrenceInstance(
      EventId id, EventExternalId newExternalId, boolean isRecurrenceOverride,
      DataSource dataSource);

  void delete(EventId id);

  void deleteByExternalId(EventExternalId externalId);

  void tryDeleteByExternalId(EventExternalId externalId);

  void batchCreate(Collection<EventCreateRequest> requests);

  void batchUpdate(Collection<EventUpdateRequest> requests);

  void batchDelete(Collection<EventId> ids);

  Optional<AccountId> getAccountId(EventId id);

  void checkin(EventId id, DataSource dataSource);

  void checkout(EventId id, DataSource dataSource);
}
