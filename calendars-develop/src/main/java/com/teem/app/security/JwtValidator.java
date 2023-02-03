package com.UoU.app.security;

import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.stereotype.Service;

@Service
class JwtValidator implements OAuth2TokenValidator<Jwt> {
  private final DelegatingOAuth2TokenValidator<Jwt> validators;

  public JwtValidator(@NonNull JwtConfig config) {
    // Build a list of validators to run through for each JWT:
    this.validators = new DelegatingOAuth2TokenValidator<>(
        // require iat:
        new JwtClaimValidator<Instant>(JwtClaimNames.IAT, x -> x != null),
        // require exp:
        new JwtClaimValidator<Instant>(JwtClaimNames.EXP, x -> x != null),
        // validate exp and nbf times allowing for clock drift (if non-null):
        new JwtTimestampValidator(),
        // // require aud to match config:
        new AudienceValidator(config.audience()),
        // require non-blank sub:
        new JwtClaimValidator<String>(JwtClaimNames.SUB, x -> x != null && !x.isBlank()),
        // require non-blank org_id:
        new JwtClaimValidator<String>(CustomClaims.ORG_ID, x -> x != null && !x.isBlank()));
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    return validators.validate(jwt);
  }

  @AllArgsConstructor
  private static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final String audience;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
      return Optional.ofNullable(jwt.getAudience()).map(x -> x.contains(audience)).orElse(false)
          ? OAuth2TokenValidatorResult.success()
          : OAuth2TokenValidatorResult.failure(
              new OAuth2Error(
                  OAuth2ErrorCodes.INVALID_REQUEST,
                  "Invalid " + JwtClaimNames.AUD + " claim",
                  null));
    }
  }
}
