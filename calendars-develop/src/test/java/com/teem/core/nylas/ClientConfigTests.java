package com.UoU.core.nylas;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.UoU.core.SecretString;
import org.junit.jupiter.api.Test;

class ClientConfigTests {

  @Test
  void ctor_clientIdCannotBeEmpty() {
    assertThatCode(() -> new ClientConfig(new SecretString(""), new SecretString("secret"), null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void ctor_clientSecretCannotBeEmpty() {
    assertThatCode(() -> new ClientConfig(
        new SecretString("id"), new SecretString(""), "https://example.com"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
