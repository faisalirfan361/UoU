package com.UoU.infra.encryption;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.UoU.core.SecretString;
import java.security.InvalidKeyException;
import org.junit.jupiter.api.Test;

class ConfigTests {

  @Test
  public void config_shouldThrowWithNullOrBlankKey() {
    assertThatCode(() -> new Config(null))
        .isInstanceOf(InvalidKeyException.class);
    assertThatCode(() -> new Config(new SecretString(" ")))
        .isInstanceOf(InvalidKeyException.class);
  }
}
