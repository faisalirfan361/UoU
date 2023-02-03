package com.UoU.core.calendars;

import static com.UoU.core.calendars.CalendarConstraints.INTERNAL_CALENDAR_BATCH_MAX;

import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import com.UoU.core.OrgMatcher;
import com.UoU.core.PageParams;
import com.UoU.core.PagedItems;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import com.UoU.core.validation.ValidatorWrapper;
import com.UoU.core.validation.ViolationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CalendarService {
  private final AccountRepository accountRepo;
  private final CalendarRepository calendarRepo;
  private final ValidatorWrapper validator;
  private final NylasTaskScheduler nylasTaskScheduler;
  private final InternalCalendarsConfig internalCalendarsConfig;

  public PagedItems<Calendar> listByAccount(
      OrgId orgId, AccountId accountId, boolean includeReadOnly, PageParams page) {
    return calendarRepo.listByAccount(orgId, accountId, includeReadOnly, page);
  }

  public Calendar get(OrgId orgId, CalendarId id) {
    return Fluent.of(calendarRepo.get(id))
        .also(x -> OrgMatcher.matchOrThrowNotFound(x.orgId(), orgId, Calendar.class))
        .get();
  }

  /**
   * Creates a single internal calendar and schedules the export to Nylas.
   */
  public InternalCalendarInfo createInternal(InternalCalendarCreateRequest request) {
    validator.validateAndThrow(request);

    val createRequest = CalendarCreateRequest.builder()
        .id(CalendarId.create())
        .orgId(request.orgId())
        .name(request.name())
        .timezone(request.timezone())
        .isReadOnly(false)
        .build();

    calendarRepo.create(createRequest);
    nylasTaskScheduler.exportCalendarsToNylas(List.of(createRequest.id()), false);

    return toInternalCalendarInfo(createRequest);
  }

  /**
   * Creates a batch of internal calendars and schedules the export to Nylas.
   */
  public Map<Integer, InternalCalendarInfo> batchCreateInternal(
      InternalCalendarBatchCreateRequest request) {

    validator.validateAndThrow(request);

    val numbers = IntStream
        .iterate(request.start(), i -> i + request.increment())
        .takeWhile(i -> request.increment() < 0 ? i >= request.end() : i <= request.end())
        .limit(INTERNAL_CALENDAR_BATCH_MAX + 1) // +1 so we can tell if we're over the max
        .toArray();

    if (numbers.length == 0 || numbers.length > INTERNAL_CALENDAR_BATCH_MAX) {
      throw new ValidationException("Between 1 and %s calendars can be created per batch".formatted(
          INTERNAL_CALENDAR_BATCH_MAX));
    }

    val ids = new ArrayList<CalendarId>();
    val createRequests = new ArrayList<CalendarCreateRequest>();
    val results = new LinkedHashMap<Integer, InternalCalendarInfo>();

    for (val num : numbers) {
      val createRequest = CalendarCreateRequest.builder()
          .id(CalendarId.create())
          .orgId(request.orgId())
          .name(request.formatName(num))
          .timezone(request.timezone())
          .isReadOnly(false)
          .build();

      ids.add(createRequest.id());
      createRequests.add(createRequest);
      results.put(num, toInternalCalendarInfo(createRequest));
    }

    if (request.isDryRun()) {
      log.debug("DRY RUN: Skipping creation of {} internal calendars", createRequests.size());
    } else {
      calendarRepo.batchCreate(createRequests);
      nylasTaskScheduler.exportCalendarsToNylas(ids, false);
    }

    return results;
  }

  /**
   * Updates a calendar, enforcing the change rules for internal and external calendars.
   *
   * <p>For internal calendars, name and timezone can be changed. For external calendars (that sync
   * to Google/MS), only timezone can be changed, and only for providers that don't give us the
   * timezone (MS).
   *
   * <p>In all cases, all fields can be passed, and changes will be checked against the current
   * calendar. Fields will only be updated when they change, and validation errors will only occur
   * when actually changing a field that is not allowed to change.
   */
  public void update(CalendarUpdateRequest request) {
    validator.validateAndThrow(request);

    val calendar = calendarRepo.get(request.id());
    calendar.getAccessInfo()
        .requireOrgOrThrowNotFound(request.orgId())
        .requireWritable();

    val account = Optional
        .ofNullable(calendar.accountId())
        .map(x -> accountRepo.get(calendar.accountId()));

    request = request.withMatchingUpdateFieldsRemoved(calendar);

    // Build a list of fields that can only be set in provider for a validation error if needed.
    val onlySetInProviderFields = new HashSet<CalendarUpdateRequest.UpdateField>();

    // Ensure timezone is not changed if the provider sets the timezone (like for Google).
    if (request.hasUpdate(CalendarUpdateRequest.UpdateField.TIMEZONE)
        && account.filter(x -> x.isProviderWithCalendarTimezones()).isPresent()) {
      onlySetInProviderFields.add(CalendarUpdateRequest.UpdateField.TIMEZONE);
    }

    // Ensure non-internal (MS/Google) calendars can only have timezone changed.
    // If no account, that is also an internal calendar that hasn't been exported to Nylas yet.
    val isInternal = account.filter(x -> !x.isInternal()).isEmpty();
    if (!isInternal) {
      request.updateFields().stream()
          .filter(x -> x != CalendarUpdateRequest.UpdateField.TIMEZONE)
          .forEach(onlySetInProviderFields::add);
    }

    if (!onlySetInProviderFields.isEmpty()) {
      throw ViolationException.forFields(
          onlySetInProviderFields.stream().map(x -> x.toString().toLowerCase(Locale.ROOT)).toList(),
          "can only be changed in the calendar provider (%s) for this calendar".formatted(
              account.orElseThrow().provider().getDisplayName()));
    }

    calendarRepo.update(request); // only updates if there are changes

    // For internal calendars, export changes to Nylas. Other calendar types are import only.
    if (isInternal && request.hasUpdates()) {
      nylasTaskScheduler.exportCalendarsToNylas(List.of(calendar.id()), false);
    }

    // Sync events if calendar timezone changed, so we can update all-day event timestamps.
    if (request.hasUpdate(CalendarUpdateRequest.UpdateField.TIMEZONE)
        && calendar.isEligibleToSync()) {
      nylasTaskScheduler.syncAllEvents(calendar.accountId(), calendar.id(), true);
    }
  }

  /**
   * Deletes an internal calendar.
   *
   * <p>Since this only supports internal calendars currently, and internal accounts only have one
   * calendar each, this operation will be functionally the same as calling
   * {@link com.UoU.core.accounts.AccountService#delete(OrgId, AccountId)} with the accountId. But
   * accounts can usually be ignored for internal calendars, so this method will usually make more
   * sense. Also, if the calendar has not been exported to Nylas yet, no accountId will exist.
   */
  public void deleteInternal(OrgId orgId, CalendarId id) {
    val calendar = calendarRepo.get(id);
    calendar.getAccessInfo()
        .requireOrgOrThrowNotFound(orgId)
        .requireWritable();

    val account = Optional
        .ofNullable(calendar.accountId())
        .map(x -> accountRepo.get(calendar.accountId()));

    // Currently, only internal calendars can be deleted. If there is no accountId, the calendar is
    // internal but awaiting export to Nylas, so can be deleted.
    if (account.filter(x -> !x.isInternal()).isPresent()) {
      throw new IllegalOperationException(
          "This calendar can only be deleted in the calendar provider (%s).".formatted(
              account.orElseThrow().provider().getDisplayName()));
    }

    // If account exists, internal calendar has been exported to Nylas, so delete entire account.
    // Otherwise, calendar hasn't been exported, and we only need to delete the calendar itself.
    account.map(x -> x.id()).ifPresentOrElse(
        accountId -> {
          accountRepo.delete(accountId);
          nylasTaskScheduler.deleteAccountFromNylas(account.orElseThrow().id());
        },
        () -> calendarRepo.delete(calendar.id()));
  }

  private InternalCalendarInfo toInternalCalendarInfo(CalendarCreateRequest request) {
    return new InternalCalendarInfo(
        request.id(),
        request.name(),
        internalCalendarsConfig.getEmail(request.id()));
  }
}
