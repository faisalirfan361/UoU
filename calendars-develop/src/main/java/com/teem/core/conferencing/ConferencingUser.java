package com.UoU.core.conferencing;

import com.UoU.core.OrgId;
import com.UoU.core.auth.AuthMethod;
import java.time.Instant;
import javax.validation.constraints.NotNull;

public record ConferencingUser(
    @NotNull ConferencingUserId id,
    @NotNull OrgId orgId,
    @NotNull String email,
    @NotNull String name,
    @NotNull AuthMethod authMethod,
    @NotNull Instant expireAt,
    @NotNull Instant createdAt,
    Instant updatedAt
) {

  public ConferencingUser {
    if (authMethod == null || authMethod.getDataType() != AuthMethod.DataType.CONFERENCING) {
      throw new IllegalArgumentException("Invalid auth method for conferencing: " + authMethod);
    }
  }
}
