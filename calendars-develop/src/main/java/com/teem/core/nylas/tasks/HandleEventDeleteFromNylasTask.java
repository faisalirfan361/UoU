package com.UoU.core.nylas.tasks;

import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventPublisher;
import com.UoU.core.events.EventRepository;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.ExternalEtagRepository;
import com.UoU.core.nylas.InboundSyncLocker;
import java.util.HashSet;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Inbound: Deletes local event in response to Nylas deleting the event.
 *
 * <p>This uses {@link com.UoU.core.nylas.InboundSyncLocker} to skip sync when another major
 * inbound sync is occurring, which will help prevent race conditions and unnecessary operations.
 */
@Service
@AllArgsConstructor
@Slf4j
public class HandleEventDeleteFromNylasTask implements Task<HandleEventDeleteFromNylasTask.Params> {
  private final EventRepository eventRepo;
  private final ExternalEtagRepository etagRepo;
  private final EventPublisher eventPublisher;
  private final InboundSyncLocker inboundSyncLocker;

  public record Params(
      @NonNull AccountId accountId,
      @NonNull EventExternalId externalId
  ) {
  }

  @Override
  public void run(Params params) {
    if (inboundSyncLocker.isAccountLocked(params.accountId())) {
      if (log.isDebugEnabled()) {
        log.debug("Inbound sync locked for {}. Skipping: Delete local event {}",
            params.accountId(), params.externalId());
      }
      return;
    }

    try {
      val coreIds = eventRepo.getCoreIds(params.externalId());

      // Get all ids, including recurrence instances, to publish events and delete etags.
      val allIds = new HashSet<>(List.of(coreIds.id()));
      val allExternalIds = new HashSet<EventExternalId>();
      coreIds.externalId().ifPresent(allExternalIds::add);
      eventRepo.listRecurrenceInstanceIdPairs(coreIds.id()).forEach(pair -> {
        allIds.add(pair.getLeft());
        pair.getRight().ifPresent(allExternalIds::add);
      });

      eventRepo.delete(coreIds.id()); // will also delete recurrence instances
      eventPublisher.eventDeleted(
          coreIds.orgId(), coreIds.calendarId(), allIds, DataSource.PROVIDER);
      etagRepo.tryDelete(allExternalIds);

      log.debug("Deleted local event: {}", coreIds.id());
    } catch (NotFoundException ex) {
      // If not found, consider that success so delete task is idempotent.
      // Log as debug (not info) because this happens often when an event is deleted from API side
      // because nylas then also sends a webhook about the delete, after the event is gone.
      log.debug("Delete of local event failed because it was not found: {}", params.externalId());
    }
  }
}
