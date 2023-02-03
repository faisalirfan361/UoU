package com.UoU.app.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.UoU._helpers.TestData;
import com.UoU.core.Fluent;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class PrincipalProviderTests {
  private static final Jwt JWT = Jwt
      .withTokenValue("test")
      .header("typ", "JWT")
      .subject(TestData.uuidString())
      .build();

  @Test
  void current_shouldWorkWhenSecurityContextIsAuthenticated() {
    var provider = new PrincipalProvider();
    SecurityContextHolder.setContext(
        new SecurityContextImpl(Fluent
            .of(new JwtAuthenticationToken(JWT))
            .also(x -> x.setAuthenticated(true))
            .get()));

    assertThat(provider.current().subject()).isEqualTo(JWT.getSubject());
  }

  @Test
  void current_shouldThrowWhenSecurityContextIsNotAuthenticated() {
    var provider = new PrincipalProvider();
    SecurityContextHolder.setContext(
        new SecurityContextImpl(Fluent
            .of(new JwtAuthenticationToken(JWT))
            .also(x -> x.setAuthenticated(false)) // not authenticated
            .get()));

    assertThatCode(() -> provider.current()).isInstanceOf(InvalidBearerTokenException.class);
  }

  @Test
  void current_shouldThrowWhenSecurityContextIsNotSet() {
    var provider = new PrincipalProvider();
    assertThatCode(() -> provider.current()).isInstanceOf(InvalidBearerTokenException.class);
  }
}
