package com.UoU.infra.encryption;

import com.UoU.core.SecretString;
import java.security.InvalidKeyException;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the {@link Encryptor}.
 */
@ConfigurationProperties("encryption")
public record Config(SecretString secretKey) {

  @SneakyThrows
  public Config {
    if (secretKey == null || secretKey.isBlank()) {
      throw new InvalidKeyException("Encryptor key cannot be empty");
    }
  }
}
