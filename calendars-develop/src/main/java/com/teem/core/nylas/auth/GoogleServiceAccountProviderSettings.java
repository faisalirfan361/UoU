package com.UoU.core.nylas.auth;

import com.nylas.ProviderSettings;

/**
 * Creates provider settings for Google service accounts that use a JSON key file.
 *
 * <p>See Nylas <a href="https://developer.nylas.com/docs/api#post/connect/authorize">API docs</a>
 * and <a href="https://developer.nylas.com/docs/the-basics/provider-guides/google/google-workspace-service-accounts">
 * Google provider guide</a>.
 *
 * <p>Example settings in authorize request:
 * <pre>{@code
 * {
 *   ...other request properties...,
 *   "settings": {
 *     "service_account_json": { # JSON from Google console
 *       "type": "service_account",
 *       "project_id": "my-google-app-123456",
 *       "private_key_id": "68915b4e55baac9191dd32e0be784687c6873b14",
 *       "private_key": "-----BEGIN PRIVATE KEY-----\nMIIE....fZ1F8=\n-----END PRIVATE KEY-----\n",
 *       "client_email": "test-service-account@my-google-app-123456.iam.gserviceaccount.com",
 *       "client_id": "10569134234239528168761",
 *       "auth_uri": "https://accounts.google.com/o/oauth2/auth",
 *       "token_uri": "https://oauth2.googleapis.com/token",
 *       "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
 *       "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/..."
 *     }
 *   }
 * }
 * }</pre>
 */
class GoogleServiceAccountProviderSettings extends ProviderSettings {
  public GoogleServiceAccountProviderSettings() {
    super("gmail");
  }

  @Override
  protected void validate() {
    assertSetting("service_account_json", "Google service_account_json is required");
  }
}
