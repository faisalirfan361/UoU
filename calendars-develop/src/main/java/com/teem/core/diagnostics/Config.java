package com.UoU.core.diagnostics;

import java.time.Duration;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("diagnostics")
public record Config(
    @NonNull Duration currentRunDuration,
    @NonNull Duration resultsExpiration,
    @NonNull ProviderSyncWait providerSyncWait
) {

  public Config {
    if (currentRunDuration.isNegative()) {
      throw new IllegalArgumentException("Invalid currentRunDuration");
    }

    if (resultsExpiration.isNegative()) {
      throw new IllegalArgumentException("Invalid resultsExpiration");
    }
  }

  public record ProviderSyncWait(
      int attempts,
      @NonNull Duration delay
  ) {

    public ProviderSyncWait {
      if (attempts <= 0) {
        throw new IllegalArgumentException("Invalid attempts");
      }

      if (delay.isNegative()) {
        throw new IllegalArgumentException("Invalid delay");
      }
    }
  }
}
