package com.UoU.core.accounts;

import com.UoU.core.OrgId;
import java.time.Instant;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public record ServiceAccountUpdateRequest(
    @Valid @NotNull ServiceAccountId id,
    @Valid @NotNull OrgId orgId,
    @Valid @NotNull Map<String, Object> settings,
    Instant settingsExpireAt) {

  @lombok.Builder(builderClassName = "Builder")
  public ServiceAccountUpdateRequest {
  }
}
