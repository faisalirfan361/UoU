package com.UoU.core.conferencing;

import com.UoU.core.WrappedValue;
import java.util.UUID;
import lombok.NonNull;

public record ConferencingUserId(@NonNull UUID value) implements WrappedValue<UUID> {

  public static ConferencingUserId create() {
    return new ConferencingUserId(UUID.randomUUID());
  }
}
