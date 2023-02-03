package com.UoU.core.accounts;

import com.UoU.core.Auditable;
import com.UoU.core.OrgId;
import com.UoU.core.auth.AuthMethod;
import java.time.Instant;
import javax.validation.constraints.NotNull;

public record ServiceAccount(
    @NotNull ServiceAccountId id,
    @NotNull OrgId orgId,
    @NotNull String email,
    @NotNull AuthMethod authMethod,
    @NotNull Instant createdAt,
    Instant updatedAt)
    implements Auditable {

  public ServiceAccount {
    if (authMethod == null
        || authMethod.getDataType() != AuthMethod.DataType.CALENDAR
        || !authMethod.isForServiceAccounts()) {
      throw new IllegalArgumentException("Invalid auth method for service account: " + authMethod);
    }
  }
}

