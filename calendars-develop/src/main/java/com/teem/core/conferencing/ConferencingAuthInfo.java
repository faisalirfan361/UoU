package com.UoU.core.conferencing;

import com.UoU.core.SecretString;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.NonNull;

/**
 * Info required for external conferencing API calls and related OAuth calls.
 */
public record ConferencingAuthInfo(
    @NonNull String name,
    @NonNull SecretString refreshToken,
    @NonNull SecretString accessToken,
    @NonNull Instant expiresAt) {

  private static final int EXPIRE_BUFFER_MINUTES = 5;

  public boolean shouldRefresh() {
    return expiresAt.minus(EXPIRE_BUFFER_MINUTES, ChronoUnit.MINUTES).isBefore(Instant.now());
  }
}
