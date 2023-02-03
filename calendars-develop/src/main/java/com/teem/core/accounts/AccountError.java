package com.UoU.core.accounts;

import java.time.Instant;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public record AccountError(
    @NotNull UUID id,
    @NotNull AccountId accountId,
    @NotNull Instant createdAt,
    @NotNull Type type,
    @NotNull String message,
    String details
) {

  public AccountError(
      @NotNull AccountId accountId,
      @NotNull Type type,
      @NotNull String message,
      String details) {
    this(UUID.randomUUID(), accountId, Instant.now(), type, message, details);
  }

  public enum Type {
    AUTH
  }
}
