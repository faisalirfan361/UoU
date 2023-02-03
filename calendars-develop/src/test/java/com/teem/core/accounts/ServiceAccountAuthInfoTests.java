package com.UoU.core.accounts;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatCode;

import com.UoU._helpers.TestData;
import com.UoU.core.auth.AuthMethod;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ServiceAccountAuthInfoTests {

  @Test
  void ctor_shouldRequireServiceAccountAuthMethod() {
    assertThatCode(() -> new ServiceAccountAuthInfo(
        TestData.serviceAccountId(),
        TestData.orgId(),
        AuthMethod.GOOGLE_OAUTH, // invalid
        Map.of("test", "test")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("auth method");
  }
}
