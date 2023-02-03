package com.UoU.core.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Provides configured OAuthHandlers based on AuthMethods.
 */
@Service
public class OauthHandlerProvider {
  private final Map<AuthMethod, OauthHandler> handlersByMethod;

  public OauthHandlerProvider(List<OauthHandler> handlers) {
    handlersByMethod = new HashMap<>();
    handlers.forEach(handler -> handler.methods().forEach(method -> {
      if (handlersByMethod.containsKey(method)) {
        throw new IllegalArgumentException(
            "Found multiple Oauth handlers for auth method: " + method.getValue());
      }
      handlersByMethod.put(method, handler);
    }));
  }

  public OauthHandler provide(AuthMethod method) {
    return Optional
        .ofNullable(handlersByMethod.get(method))
        .orElseThrow(() -> new IllegalArgumentException(
            "No OAuth handler for auth method: " + method.getValue()));
  }
}
