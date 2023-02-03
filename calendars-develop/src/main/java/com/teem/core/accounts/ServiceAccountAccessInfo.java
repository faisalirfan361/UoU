package com.UoU.core.accounts;

import com.UoU.core.AccessInfo;
import com.UoU.core.OrgId;
import javax.validation.constraints.NotNull;

public record ServiceAccountAccessInfo(
    @NotNull OrgId orgId
) implements AccessInfo<ServiceAccountAccessInfo> {
  private static final String NAME = "ServiceAccount";

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public boolean isReadOnly() {
    return false; // service accounts are never read-only
  }
}
