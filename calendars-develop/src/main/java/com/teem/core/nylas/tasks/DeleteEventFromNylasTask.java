package com.UoU.core.nylas.tasks;

import com.nylas.RequestFailedException;
import com.UoU.core.Task;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.nylas.ExternalEtagRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Outbound: Deletes event from Nylas after it's already been deleted locally.
 */
@Service
@AllArgsConstructor
@Slf4j
public class DeleteEventFromNylasTask implements Task<DeleteEventFromNylasTask.Params> {
  private final EventHelper eventHelper;
  private final ExternalEtagRepository etagRepo;

  public record Params(
      @NonNull AccountId accountId,
      @NonNull EventExternalId externalId
  ) {
  }

  @Override
  @SneakyThrows
  public void run(Params params) {
    val client = eventHelper.createNylasClient(params.accountId());

    try {
      client.events().delete(params.externalId().value(), true);
      log.debug("Deleted event from Nylas: {}", params.externalId());
    } catch (RequestFailedException ex) {
      if (Exceptions.isNotFound(ex)) {
        // If not found, consider that success so delete task is idempotent.
        log.info("Delete from Nylas failed because event was not found: {}", params.externalId());
      } else {
        throw ex;
      }
    } finally {
      // Always try to delete the external etag.
      // If the event is a recurrence master, we're not able to lookup the instance external ids and
      // delete the instance etags since the db events are usually deleted before we get here. This
      // should be ok since they'll just expire, but if we want to solve this edge case in the
      // future, we could get the instance ids from Nylas or cache the ids before deleting the
      // events from the db.
      etagRepo.tryDelete(params.externalId());
    }
  }
}
