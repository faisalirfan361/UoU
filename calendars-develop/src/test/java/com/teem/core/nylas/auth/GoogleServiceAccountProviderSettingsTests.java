package com.UoU.core.nylas.auth;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

public class GoogleServiceAccountProviderSettingsTests {

  @Test
  void validate_shouldValidate() {
    val settings = buildValidSettings();
    assertThatCode(() -> settings.validate()).doesNotThrowAnyException();
  }

  @Test
  void validate_shouldThrowForMissingKey() {
    val settings = buildValidSettings();
    settings.getValidatedSettings().remove("service_account_json");

    assertThatCode(() -> settings.validate())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("service_account_json");
  }

  private GoogleServiceAccountProviderSettings buildValidSettings() {
    val settings = new GoogleServiceAccountProviderSettings();
    settings.add("service_account_json", Map.of("test", "value"));
    return settings;
  }
}
