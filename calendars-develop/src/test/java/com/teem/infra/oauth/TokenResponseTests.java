package com.UoU.infra.oauth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.UoU.core.auth.OauthException;
import lombok.val;
import org.junit.jupiter.api.Test;

class TokenResponseTests {

  @Test
  void getEmailFromIdToken_shouldParseJwt() {
    val idToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20ifQ."
        + "T4ia8uMh-Xd9ldq1VNBc20ZOvwOWnWM7yGwwITXa5gc";
    val response = new TokenResponse(idToken, "refresh", "access", null);

    val email = response.getEmailFromIdToken();

    assertThat(email).isEqualTo("test@example.com");
  }

  @Test
  void getEmailFromIdToken_shouldThrowForInvalidJwt() {
    val response = new TokenResponse("invalid_id_token", "refresh", "access", null);

    assertThatCode(() -> response.getEmailFromIdToken())
        .isInstanceOf(OauthException.class);
  }

  @Test
  void getEmailFromIdToken_shouldThrowForBlankEmail() {
    val idToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0IiwiZW1haWwiOiIgIn0."
        + "ierkBjmaUPsadSQHb3_VeoEUsjQHsJkciq0A26pBCQ8";
    val response = new TokenResponse(idToken, "refresh", "access", 100);

    assertThatCode(() -> response.getEmailFromIdToken())
        .isInstanceOf(OauthException.class);
  }

  @Test
  void getNameFromIdToken_shouldParseJwt() {
    val idToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InhAZXhhbXBsZS5jb20iLCJuYW1lIjo"
        + "idGVzdCJ9.jkcpIhhTzBlWwaKIvhLgxAExd_Q0oBw_j7XEM2AeShI";
    val response = new TokenResponse(idToken, "refresh", "access", null);

    val name = response.getNameFromIdToken();

    assertThat(name).isEqualTo("test");
  }
}
