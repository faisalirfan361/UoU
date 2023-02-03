package com.UoU.core.auth.serviceaccountsettings;

import com.UoU.core.Fluent;
import com.UoU.core.auth.AuthInput;
import com.UoU.core.auth.AuthMethod;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.ValidationException;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * Creates and handles parsing for Google service account settings (GOOGLE_SA auth method).
 *
 * <p>Example service account JSON key from Google Cloud console:
 * <pre>{@code
 * {
 *   "type": "service_account",
 *   "project_id": "my-google-app-123456",
 *   "private_key_id": "68915b4e55baac9191dd32e0be784687c6873b14",
 *   "private_key": "-----BEGIN PRIVATE KEY-----\nMIIE....fZ1F8=\n-----END PRIVATE KEY-----\n",
 *   "client_email": "test-service-account@my-google-app-123456.iam.gserviceaccount.com",
 *   "client_id": "10569134234239528168761",
 *   "auth_uri": "https://accounts.google.com/o/oauth2/auth",
 *   "token_uri": "https://oauth2.googleapis.com/token",
 *   "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
 *   "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/..."
 * }
 * }</pre>
 */
@Service
public class GoogleJsonSettingsHandler implements AuthSettingsHandler {
  private static final String WRAPPER_KEY = "service_account_json";
  private static final String EMAIL_KEY = "client_email";

  private static final Map<String, Object> REQUIRED_VALUES = Fluent
      .of(new LinkedHashMap<String, Object>())
      .also(x -> x.put("type", "service_account"))
      .also(x -> x.put("project_id", Matcher.NonBlankString))
      .also(x -> x.put("private_key_id", Matcher.NonBlankString))
      .also(x -> x.put("private_key", Matcher.NonBlankString))
      .also(x -> x.put(EMAIL_KEY, Matcher.Email))
      .also(x -> x.put("client_id", Matcher.NonBlankString))
      .also(x -> x.put("auth_uri", Matcher.Uri))
      .also(x -> x.put("token_uri", Matcher.Uri))
      .also(x -> x.put("auth_provider_x509_cert_url", Matcher.Uri))
      .also(x -> x.put("client_x509_cert_url", Matcher.Uri))
      .map(Collections::unmodifiableMap)
      .get();

  @Override
  public AuthMethod authMethod() {
    return AuthMethod.GOOGLE_SA;
  }

  /**
   * Creates service account settings for a user-submitted Google JSON key, which never expires.
   */
  @Override
  public AuthSettings createSettings(AuthInput authInput) {
    validate(authInput.getDirectlySubmittedAuthData());

    return new AuthSettings(
        (String) authInput.getDirectlySubmittedAuthData().get(EMAIL_KEY),
        Map.of(WRAPPER_KEY, authInput.getDirectlySubmittedAuthData()));
  }

  /**
   * Validate the Google service account json.
   *
   * <p>This does some sanity-check validation to make sure the JSON seems like a valid key because
   * the key may not be used right away and so we want to prevent bad data from being stored.
   */
  private void validate(Map<String, Object> authData) {
    if (authData == null || authData.isEmpty()) {
      throw new ValidationException("Google service account JSON cannot be empty");
    }

    REQUIRED_VALUES.forEach((key, valueSpec) -> {
      if (valueSpec instanceof Matcher) {
        val str = (String) authData.get(key);
        switch ((Matcher) valueSpec) {
          case NonBlankString -> {
            if (str == null || str.isBlank()) {
              throw new ValidationException(
                  "Google service account JSON '%s' cannot be empty".formatted(key));
            }
          }
          case Uri -> {
            if (str == null || !str.startsWith("https://")) {
              throw new ValidationException(
                  "Google service account JSON '%s' must be a valid https URI".formatted(key));
            }
          }
          case Email -> {
            if (str == null || !str.contains("@")) {
              throw new ValidationException(
                  "Google service account JSON '%s' must be a valid email".formatted(key));
            }
          }
          default -> throw new IllegalArgumentException("Invalid Matcher type");
        }
      } else if (!valueSpec.equals(authData.get(key))) {
        throw new ValidationException(
            "Google service account JSON '%s' must be '%s'".formatted(key, valueSpec));
      }
    });
  }

  private enum Matcher { NonBlankString, Uri, Email }
}
