package com.UoU.core.auth.serviceaccountsettings;

import static java.util.stream.Collectors.toMap;

import com.UoU.core.auth.AuthMethod;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Provides {@link AuthSettingsHandler} based on auth methods.
 */
@Service
public class AuthSettingsHandlerProvider {
  private final Map<AuthMethod, AuthSettingsHandler> handlers;

  public AuthSettingsHandlerProvider(List<AuthSettingsHandler> handlers) {
    this.handlers = handlers.stream().collect(toMap(
            x -> x.authMethod(),
            x -> x,
            (x, y) -> {
              throw new IllegalArgumentException(
                  "Found multiple AuthSettingsHandlers for auth method: "
                      + x.authMethod().getValue());
            }));
  }

  public AuthSettingsHandler provide(AuthMethod authMethod) {
    return Optional.ofNullable(handlers.get(authMethod))
        .orElseThrow(() -> new IllegalArgumentException(
            "No AuthSettingsHandler found for auth method: " + authMethod.getValue()));
  }
}
