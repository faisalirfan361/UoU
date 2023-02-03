package com.UoU.core.calendars;

import com.UoU.core.OrgId;
import com.UoU.core.PageParams;
import com.UoU.core.PagedItems;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.Provider;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public interface CalendarRepository {
  // DO-LATER: Remove this non-paged method when nylas sync doesn't rely on it and batches better.
  Stream<Calendar> listByAccount(
      OrgId orgId, AccountId accountId, boolean includeReadOnly);

  PagedItems<Calendar> listByAccount(
      OrgId orgId, AccountId accountId, boolean includeReadOnly, PageParams page);

  /**
   * Returns batches of syncable calendars that are currently at the localHour based on timezone.
   */
  Stream<List<Pair<AccountId, CalendarId>>> listSyncableCalendarsAtLocalHour(
      int localHour, int batchSize);

  boolean exists(CalendarId id);

  Calendar get(CalendarId id);

  Optional<Calendar> tryGet(CalendarId id);

  Optional<Calendar> tryGetByExternalId(CalendarExternalId externalId);

  Optional<CalendarId> tryGetId(CalendarExternalId externalId);

  Optional<CalendarExternalId> tryGetExternalId(CalendarId id);

  Optional<CalendarAccessInfo> tryGetAccessInfo(CalendarId id);

  Optional<AccountId> getAccountId(CalendarId id);

  Optional<Provider> getAccountProvider(CalendarId id);

  String getTimezone(CalendarId calendarId);

  void create(CalendarCreateRequest request);

  void update(CalendarUpdateRequest request);

  /**
   * Links the calendar to the account and external calendar.
   */
  void link(CalendarId id, AccountId accountId, CalendarExternalId externalId);

  void delete(CalendarId id);

  void batchCreate(List<CalendarCreateRequest> requests);

  void batchUpdate(List<CalendarUpdateRequest> requests);

  void batchDelete(List<CalendarId> ids);
}
