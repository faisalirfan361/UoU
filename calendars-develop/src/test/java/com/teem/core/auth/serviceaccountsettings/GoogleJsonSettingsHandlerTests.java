package com.UoU.core.auth.serviceaccountsettings;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.UoU.core.auth.AuthInput;
import java.util.HashMap;
import java.util.Map;
import javax.validation.ValidationException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GoogleJsonSettingsHandlerTests {
  private static GoogleJsonSettingsHandler HANDLER = new GoogleJsonSettingsHandler();


  @Test
  void createSettings_shouldThrowForEmptyJson() {
    val input = AuthInput.ofDirectlySubmittedAuthData(Map.of());

    assertThatCode(() -> HANDLER.createSettings(input))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("service account JSON");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "type=invalid",
      "project_id=",
      "private_key_id= ",
      "private_key= ",
      "client_email=invalid",
      "client_id= ",
      "auth_uri=invalid",
      "token_uri=http://invalid",
      "auth_provider_x509_cert_url=ftp://invalid",
      "client_x509_cert_url=/invalid",
  })
  void createSettings_shouldValidateJsonProperties(String input) {
    val keyValuePair = input.split("=", 2);
    val json = createValidJson();
    json.put(keyValuePair[0], keyValuePair[1]);
    val authInput = AuthInput.ofDirectlySubmittedAuthData(json);

    assertThatCode(() -> HANDLER.createSettings(authInput))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining(keyValuePair[0]);
  }

  private static HashMap<String, Object> createValidJson() {
    return new HashMap<>(Map.of(
        "type", "service_account",
        "project_id", "test",
        "private_key_id", "test",
        "private_key", "test",
        "client_email", "x@y.z",
        "client_id", "test",
        "auth_uri", "https://example.com",
        "token_uri", "https://example.com",
        "auth_provider_x509_cert_url", "https://example.com",
        "client_x509_cert_url", "https://example.com"
    ));
  }
}
