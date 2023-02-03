package com.UoU.core.events;

import com.UoU.core.WrappedValue;
import java.util.UUID;
import lombok.NonNull;

public record EventId(@NonNull UUID value) implements WrappedValue<UUID> {
  public static EventId create() {
    return new EventId(UUID.randomUUID());
  }
}
