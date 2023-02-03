package com.UoU.app.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.UoU._helpers.TestData;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

class JwtValidatorTests {
  private static final JwtConfig CONFIG = new JwtConfig("test-audience", "test-public-key");
  private static final JwtValidator VALIDATOR = new JwtValidator(CONFIG);

  @Test
  void validate_shouldPass() {
    var jwt = buildJwt().build();
    var result = VALIDATOR.validate(jwt);
    assertThat(result.hasErrors()).isFalse();
  }

  @Test
  void validate_shouldRequireAudienceToMatchConfig() {
    validateAndAssertErrorContains(
        buildJwt().audience(List.of("invalid-audience")).build(),
        JwtClaimNames.AUD);
  }

  @Test
  void validate_shouldRequireNonNullExpiration() {
    validateAndAssertErrorContains(
        buildJwt().expiresAt(null).build(),
        JwtClaimNames.EXP);
  }

  @Test
  void validate_shouldThrowWhenExpirationIsNotInstant() {
    assertThatCode(() -> VALIDATOR.validate(buildJwt().claim(JwtClaimNames.EXP, "123").build()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validate_shouldValidateExpirationTime() {
    // We allow for 60 seconds of clock drift, so move date 61 seconds into the past.
    validateAndAssertErrorContains(
        buildJwt().expiresAt(Instant.now().minusSeconds(61)).build(),
        JwtClaimNames.EXP);
  }

  @Test
  void validate_shouldRequireNonNullIssuedAt() {
    validateAndAssertErrorContains(
        buildJwt().issuedAt(null).build(),
        JwtClaimNames.IAT);
  }

  @Test
  void validate_shouldRequireNonBlankSubject() {
    validateAndAssertErrorContains(
        buildJwt().subject(null).build(),
        JwtClaimNames.SUB);
    validateAndAssertErrorContains(
        buildJwt().subject(" ").build(),
        JwtClaimNames.SUB);
  }

  @Test
  void validate_shouldRequireNonBlankOrgId() {
    validateAndAssertErrorContains(
        buildJwt().claim(CustomClaims.ORG_ID, null).build(),
        CustomClaims.ORG_ID);
    validateAndAssertErrorContains(
        buildJwt().claim(CustomClaims.ORG_ID, " ").build(),
        CustomClaims.ORG_ID);
  }

  private static Jwt.Builder buildJwt() {
    return Jwt.withTokenValue("test")
        .header("test", "test")
        .subject(TestData.email())
        .claim(CustomClaims.ORG_ID, TestData.uuidString())
        .audience(List.of(CONFIG.audience()))
        .issuedAt(Instant.now().minusSeconds(300))
        .expiresAt(Instant.now().plusSeconds(300));
  }

  private static void validateAndAssertErrorContains(Jwt jwt, String errorContains) {
    var result = VALIDATOR.validate(jwt);
    assertThat(result.hasErrors()).isTrue();

    var errors = result.getErrors().stream().toList();
    assertThat(errors.size()).isEqualTo(1);
    assertThat(errors.get(0).getDescription()).contains(errorContains);
  }
}
