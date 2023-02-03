package com.UoU.core.tasks;

import com.UoU.core.Task;
import com.UoU.core.calendars.CalendarRepository;
import com.UoU.core.nylas.tasks.NylasTaskScheduler;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Advances the events active period for all calendars that are eligible at the current time.
 *
 * <p>Each calendar will advance at midnight local time according to the calendar timezone. When no
 * timezone is set on a calendar, the default timezone will be assumed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdvanceEventsActivePeriodTask implements Task.WithNoParams {
  private static final int SYNC_HOUR = LocalTime.MIDNIGHT.getHour();
  private static final int BATCH_SIZE = 500;
  private static final int BATCH_DELAY_SECONDS = 10;

  private final CalendarRepository calendarRepo;
  private final NylasTaskScheduler nylasTaskScheduler;
  private final BatchSpringTaskScheduler batchSpringTaskScheduler;

  @Override
  public void run() {
    // Get all calendar ids in batches and schedule syncAllEvents via batches with spring
    // taskscheduler so that we don't overwhelm our db or Nylas by running too many syncs at once.
    // Note that spring taskscheduler just uses local threads, so if the running node dies, the
    // tasks will be lost. However, since we're not scheduling very far in advance, and this should
    // be run regularly, that's an ok risk for now.
    val batches = calendarRepo.listSyncableCalendarsAtLocalHour(SYNC_HOUR, BATCH_SIZE);
    batchSpringTaskScheduler.scheduleBatches(
        batches.iterator(),
        BATCH_DELAY_SECONDS,
        batch -> batch.forEach(x -> nylasTaskScheduler.syncAllEvents(x.getLeft(), x.getRight())));
  }
}
