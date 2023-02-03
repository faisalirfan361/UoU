package com.UoU.core.nylas.tasks;

import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.nylas.InboundSyncLocker;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Inbound: Deletes local calendar in response to Nylas deleting the calendar.
 *
 * <p>This uses {@link com.UoU.core.nylas.InboundSyncLocker} to skip sync when another major
 * inbound sync is occurring, which will help prevent race conditions and unnecessary operations.
 */
@Service
@AllArgsConstructor
@Slf4j
public class HandleCalendarDeleteFromNylasTask
    implements Task<HandleCalendarDeleteFromNylasTask.Params> {

  private final CalendarRepository calendarRepo;
  private final InboundSyncLocker inboundSyncLocker;

  public record Params(
      @NonNull AccountId accountId,
      @NonNull CalendarExternalId calendarExternalId
  ) {
  }

  @Override
  public void run(Params params) {
    if (inboundSyncLocker.isAccountLocked(params.accountId())) {
      log.debug("Inbound sync locked for {}. Skipping: Delete local calendar {}",
          params.accountId(), params.calendarExternalId());
      return;
    }

    calendarRepo.tryGetId(params.calendarExternalId()).ifPresentOrElse(
        id -> {
          calendarRepo.delete(id);
          log.debug("Deleted local calendar: {}", id);
        },
        () -> log.info(
            "Delete of local calendar skipped because it was not found: {}",
            params.calendarExternalId()));
  }
}
