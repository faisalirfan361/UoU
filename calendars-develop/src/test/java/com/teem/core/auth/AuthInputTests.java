package com.UoU.core.auth;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.UoU._helpers.TestData;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class AuthInputTests {

  @Test
  void getOauthResult_throwsWhenNull() {
    val input = AuthInput.ofDirectlySubmittedAuthData(Map.of("test", "test"));

    assertThatCode(input::getOauthResult)
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void getDirectlySubmittedAuthData_throwsWhenNull() {
    val input = AuthInput.ofOauthResult(TestData.oauthResult(TestData.email()));

    assertThatCode(input::getDirectlySubmittedAuthData)
        .isInstanceOf(IllegalStateException.class);
  }
}
