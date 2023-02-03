package com.UoU.core.accounts;

import com.UoU.core.AccessInfo;
import com.UoU.core.OrgId;
import lombok.NonNull;

public record AccountAccessInfo(@NonNull OrgId orgId) implements AccessInfo<AccountAccessInfo> {
  private static final String NAME = "Account";

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public boolean isReadOnly() {
    return false; // accounts are never read-only
  }
}
