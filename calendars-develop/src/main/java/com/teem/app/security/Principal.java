package com.UoU.app.security;

import com.UoU.core.OrgId;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * A custom principal object that wraps spring's principal to provide helpers.
 */
@AllArgsConstructor
public class Principal {
  private final Jwt jwt;

  public Principal(@NonNull JwtAuthenticationToken token) {
    this.jwt = (Jwt) token.getPrincipal();
    if (this.jwt == null) {
      throw new InvalidBearerTokenException("Missing or invalid JWT");
    }
  }

  /**
   * Gets the subject claim, which is usually a user email.
   */
  public String subject() {
    return jwt.getSubject();
  }

  /**
   * Gets the organization id.
   */
  public OrgId orgId() {
    return new OrgId(getClaimAsNonBlankString(CustomClaims.ORG_ID));
  }

  /**
   * Returns a claim string or throws an exception if it's blank.
   *
   * <p>JWT validation should check all claims that make up a minimum valid JWT, but this can
   * be used to be extra sure a bad value can never be used.
   */
  private String getClaimAsNonBlankString(String claim) {
    var value = jwt.getClaimAsString(claim);
    if (value == null || value.isBlank()) {
      throw new InvalidBearerTokenException("Missing or invalid JWT claim: " + claim);
    }

    return value;
  }
}
