package com.UoU.core.auth.serviceaccountsettings;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Auth settings for a service account, which includes the account email and settings expiration.
 */
public record AuthSettings(
    String email,
    Map<String, Object> settings,
    Optional<Instant> expiration) {

  public AuthSettings(String email, Map<String, Object> settings) {
    this(email, settings, Optional.empty());
  }

  public AuthSettings(String email, Map<String, Object> settings, Instant expiration) {
    this(email, settings, Optional.of(expiration));
  }

  public AuthSettings {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }

    expiration = expiration != null ? expiration : Optional.empty();
  }
}
