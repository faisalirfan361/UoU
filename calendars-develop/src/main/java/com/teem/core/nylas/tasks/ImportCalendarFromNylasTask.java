package com.UoU.core.nylas.tasks;

import com.nylas.NylasAccount;
import com.nylas.RequestFailedException;
import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.calendars.CalendarUpdateRequest;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.InboundSyncLocker;
import com.UoU.core.nylas.NylasClientFactory;
import com.UoU.core.nylas.mapping.NylasCalendarMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Inbound: Imports calendar info from Nylas, and optionally triggers a sync of events as well.
 *
 * <p>This is a one-way sync with Nylas calendars. Since calendars are only expected to be created
 * or updated via their respective providers, we will only pull data from Nylas and should not have
 * to push any changes from the database to Nylas.
 *
 * <p>This uses {@link com.UoU.core.nylas.InboundSyncLocker} to skip sync when another major
 * inbound sync is occurring, which will help prevent race conditions and unnecessary operations.
 */
@Service
@AllArgsConstructor
@Slf4j
public class ImportCalendarFromNylasTask implements Task<ImportCalendarFromNylasTask.Params> {
  private final NylasClientFactory nylasClientFactory;
  private final CalendarRepository calendarRepo;
  private final AccountRepository accountRepo;
  private final NylasCalendarMapper mapper;
  private final NylasTaskScheduler scheduler;
  private final InboundSyncLocker inboundSyncLocker;

  public record Params(
      @NonNull AccountId accountId,
      @NonNull CalendarExternalId calendarExternalId,
      boolean includeEvents
  ) {
  }

  @Override
  public void run(Params params) {
    if (inboundSyncLocker.isAccountLocked(params.accountId())) {
      log.debug("Inbound sync locked for {}. Skipping: Import calendar {}",
          params.accountId(), params.calendarExternalId());
      return;
    }

    val account = accountRepo.get(params.accountId());
    val orgId = account.orgId();
    val token = accountRepo.getAccessToken(params.accountId());
    val client = nylasClientFactory.createAccountClient(token);

    val nylasCalendar = getNylasCalendar(client, params.calendarExternalId());
    val localCalendar = calendarRepo.tryGetByExternalId(params.calendarExternalId());
    val localId = localCalendar.map(x -> x.id()).orElseGet(CalendarId::create);
    var isTimezoneChanged = false;

    if (localCalendar.isPresent()) {
      val updateRequest = mapper.toUpdateRequestModel(nylasCalendar, localCalendar.orElseThrow());
      isTimezoneChanged = updateRequest.hasUpdate(CalendarUpdateRequest.UpdateField.TIMEZONE);
      calendarRepo.update(updateRequest);
    } else {
      val createRequest = mapper.toCreateRequestModel(nylasCalendar, localId, orgId);
      calendarRepo.create(createRequest);
    }

    // Sync events if requested and if not read-only (we don't import events for read-only).
    // Also sync events if calendar timezone changed, so we can update all-day event timestamps.
    if (!nylasCalendar.isReadOnly() && (params.includeEvents() || isTimezoneChanged)) {
      scheduler.syncAllEvents(params.accountId(), localId, isTimezoneChanged);
    }

    log.debug("Imported calendar {} for {}", params.calendarExternalId(), params.accountId());
  }

  @SneakyThrows
  private com.nylas.Calendar getNylasCalendar(
      NylasAccount client, CalendarExternalId calendarExternalId) {

    try {
      return client.calendars().get(calendarExternalId.value());
    } catch (RequestFailedException ex) {
      if (Exceptions.isNotFound(ex)) {
        throw new NotFoundException("Nylas calendar not found");
      }
      throw ex;
    }
  }
}
