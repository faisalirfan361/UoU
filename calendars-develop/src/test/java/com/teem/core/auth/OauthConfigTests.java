package com.UoU.core.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.UoU.core.SecretString;
import lombok.val;
import org.junit.jupiter.api.Test;

class OauthConfigTests {

  @Test
  void ctor_doesNotAllowNullOrEmptyValues() {
    val validCreds = new OauthConfig.OauthCredentials("id", new SecretString("secret"));

    assertThatCode(() -> new OauthConfig("https://example.com", null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("oauth");

    assertThatCode(() -> new OauthConfig("", validCreds, validCreds))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("oauth");

    assertThatCode(() -> new OauthConfig("https://example.com", validCreds, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("oauth");

    assertThatCode(() -> new OauthConfig(
        "https://example.com",
        validCreds,
        new OauthConfig.OauthCredentials(" ", new SecretString(" "))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("oauth");
  }
}
