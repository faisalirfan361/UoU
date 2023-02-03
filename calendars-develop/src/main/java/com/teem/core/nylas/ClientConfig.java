package com.UoU.core.nylas;

import com.UoU.core.SecretString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("nylas.client")
public record ClientConfig(SecretString id, SecretString secret, String uri) {
  public ClientConfig {
    if (id == null || id.isBlank() || secret == null || secret.isBlank()) {
      throw new IllegalArgumentException("Invalid nylas client config");
    }
  }
}
