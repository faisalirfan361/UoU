package com.UoU.app;

import com.UoU.core.auth.AuthResult;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Helper for controllers that authorize calendar accounts (not related to API auth).
 */
@Service
public class AuthConnectHelper {

  public static final String ACCOUNT_ID_QUERY_PARAM = "calendarsApiAccountId";
  public static final String SERVICE_ACCOUNT_ID_QUERY_PARAM = "calendarsApiServiceAccountId";
  public static final String CONFERENCING_USER_ID_QUERY_PARAM = "calendarsApiConferencingUserId";

  /**
   * Returns the redirect URI set on the auth code with an id query added, else returns empty.
   *
   * <p>If the auth code was created with a redirect URI, this parses and returns it so the user
   * can be redirected back to the calling app. If there are any parsing errors, like from an
   * invalid URI, this will return empty.
   *
   * <p>For example, if the auth code was created with a redirect URI of "https://example.com",
   * this would return "https://example.com?calendarsApiAccountId=[id]" or
   * "https://example.com?calendarsApiServiceAccountId=[id]" depending on the account type.
   */
  public Optional<String> getAuthSuccessRedirectUri(AuthResult authResult) {
    try {
      return Optional
          .ofNullable(authResult.code().redirectUri())
          .map(uri -> UriComponentsBuilder
              .fromUriString(uri)
              .queryParam(
                  switch (authResult.idType()) {
                    case ACCOUNT -> ACCOUNT_ID_QUERY_PARAM;
                    case SERVICE_ACCOUNT -> SERVICE_ACCOUNT_ID_QUERY_PARAM;
                    case CONFERENCING_USER -> CONFERENCING_USER_ID_QUERY_PARAM;
                  }, authResult.id())
              .toUriString())
          .filter(str -> str.startsWith("http://") || str.startsWith("https://"));
    } catch (Exception ex) {
      return Optional.empty();
    }
  }
}
