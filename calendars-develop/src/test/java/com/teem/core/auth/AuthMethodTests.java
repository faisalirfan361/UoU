package com.UoU.core.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class AuthMethodTests {

  @Test
  void byStringValue_shouldWork() {
    var result = AuthMethod.byStringValue("ms-oauth-sa").orElseThrow();
    assertThat(result).isEqualTo(AuthMethod.MS_OAUTH_SA);
  }
}
