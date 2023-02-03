package com.UoU.core.calendars;

import com.UoU.core.Auditable;
import com.UoU.core.OrgId;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.ReadOnlyException;
import java.time.Instant;
import java.util.Optional;
import javax.validation.constraints.NotNull;

public record Calendar(
    @NotNull CalendarId id,
    CalendarExternalId externalId,
    AccountId accountId,
    @NotNull OrgId orgId,
    @NotNull String name,
    boolean isReadOnly,
    String timezone,
    @NotNull Instant createdAt,
    Instant updatedAt)
    implements Auditable {

  public CalendarAccessInfo getAccessInfo() {
    return new CalendarAccessInfo(orgId, isReadOnly);
  }

  /**
   * Returns whether the calendar has everything needed to sync.
   *
   * <p>Specifically, a calendar must be writable and have externalId and accountId to sync.
   */
  public boolean isEligibleToSync() {
    return getIsEligibleToSyncException().isEmpty();
  }

  /**
   * Throws an IllegalOperationException if the calendar does not have everything needed to sync.
   *
   * <p>Specifically, a calendar must be writable and have externalId and accountId to sync.
   */
  public void requireIsEligibleToSync() {
    getIsEligibleToSyncException().ifPresent(ex -> {
      throw ex;
    });
  }

  private Optional<IllegalOperationException> getIsEligibleToSyncException() {
    if (isReadOnly) {
      return Optional.of(
          new ReadOnlyException("Cannot sync read-only calendar"));
    }

    if (externalId == null || externalId.value().isBlank()) {
      return Optional.of(
          new IllegalOperationException("Cannot sync calendar without an external id"));
    }

    if (accountId == null || accountId.value().isBlank()) {
      return Optional.of(
          new IllegalOperationException("Cannot sync calendar without an account"));
    }

    return Optional.empty();
  }
}
