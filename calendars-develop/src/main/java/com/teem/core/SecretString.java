package com.UoU.core;

import javax.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * Value object for secret or sensitive strings that need to be handled carefully.
 *
 * <p>This does two things:
 * - Clearly marks the string as something you should be careful with.
 * - Overloads toString() to prevent accidental logging, caching, etc.
 *
 * <p>This does *not* implement {@link WrappedValue} because we don't want mappers or
 * anything else unwrapping the value automatically based on the wrapped value pattern.
 */
public record SecretString(@NonNull @NotBlank String value) {
  @Override
  public String toString() {
    return "SecretString(***)";
  }

  public boolean isBlank() {
    return value.isBlank();
  }

  public boolean isEmpty() {
    return value.isEmpty();
  }
}
