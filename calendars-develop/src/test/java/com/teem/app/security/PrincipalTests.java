package com.UoU.app.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.UoU._helpers.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class PrincipalTests {

  @Test
  void orgId_shouldGetValueFromClaim() {
    var orgId = TestData.orgId();
    var token = new JwtAuthenticationToken(Jwt
        .withTokenValue("test")
        .header("typ", "JWT")
        .claim(CustomClaims.ORG_ID, orgId.value())
        .build());

    var result = new Principal(token).orgId();

    assertThat(result).isEqualTo(orgId);
  }

  @Test
  void orgId_shouldThrowWhenClaimIsBlank() {
    var token = new JwtAuthenticationToken(Jwt
        .withTokenValue("test")
        .header("typ", "JWT")
        .claim(CustomClaims.ORG_ID, " ")
        .build());
    var principal = new Principal(token);

    assertThatCode(() -> principal.orgId()).isInstanceOf(InvalidBearerTokenException.class);
  }
}
