package com.UoU.core.accounts;

import com.UoU.core.WrappedValue;
import java.util.UUID;
import lombok.NonNull;

public record ServiceAccountId(@NonNull UUID value) implements WrappedValue<UUID> {
  public static ServiceAccountId create() {
    return new ServiceAccountId(UUID.randomUUID());
  }
}
