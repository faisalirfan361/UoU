package com.UoU.core.accounts;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatCode;

import com.UoU._helpers.TestData;
import com.UoU.core.auth.AuthMethod;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ServiceAccountTests {
  @Test
  void ctor_shouldRequireServiceAccountAuthMethod() {
    assertThatCode(() -> new ServiceAccount(
        TestData.serviceAccountId(),
        TestData.orgId(),
        TestData.email(),
        AuthMethod.GOOGLE_OAUTH, // invalid
        Instant.now(),
        Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("auth method");
  }
}
