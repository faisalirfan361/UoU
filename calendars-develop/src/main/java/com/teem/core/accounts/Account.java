package com.UoU.core.accounts;

import com.UoU.core.Auditable;
import com.UoU.core.DataConfig;
import com.UoU.core.OrgId;
import com.UoU.core.auth.AuthMethod;
import java.time.Instant;
import javax.validation.constraints.NotNull;

public record Account(
    @NotNull AccountId id,
    ServiceAccountId serviceAccountId,
    @NotNull OrgId orgId,
    @NotNull String email,
    @NotNull String name,
    SyncState syncState,
    @NotNull AuthMethod authMethod,
    @NotNull Instant createdAt,
    Instant updatedAt
) implements Auditable {

  public Account {
    if (authMethod == null || authMethod.getDataType() != AuthMethod.DataType.CALENDAR) {
      throw new IllegalArgumentException("Invalid auth method for account: " + authMethod);
    }
  }

  public Provider provider() {
    return authMethod.getProvider();
  }

  public boolean isInternal() {
    return authMethod.getProvider() == Provider.INTERNAL;
  }

  public boolean isProviderWithCalendarTimezones() {
    return DataConfig.Calendars.TIMEZONE_PROVIDERS.contains(authMethod.getProvider());
  }
}
