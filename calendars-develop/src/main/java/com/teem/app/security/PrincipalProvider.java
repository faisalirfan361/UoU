package com.UoU.app.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Provides the current request principal via {@link SecurityContextHolder}.
 */
@Service
public class PrincipalProvider {
  public Principal current() {
    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated() || !(auth instanceof JwtAuthenticationToken)) {
      throw new InvalidBearerTokenException("Missing or invalid JWT");
    }

    return new Principal((JwtAuthenticationToken) auth);
  }
}
