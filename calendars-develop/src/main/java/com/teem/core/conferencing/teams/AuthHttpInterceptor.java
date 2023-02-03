package com.UoU.core.conferencing.teams;

import com.microsoft.graph.authentication.BaseAuthenticationProvider;
import com.UoU.core.conferencing.ConferencingAuthInfo;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * OkHttp interceptor that adds an Authorization header for a Teams user.
 *
 * <p>MS Graph SDK uses the OkHttp client, which allows interceptors for auth and other stuff. The
 * default auth interceptor doesn't work for us because it assumes you know a single user to auth
 * at the time you construct the Graph SDK client. However, since this causes one OkHttp client per
 * user, it's not very good for performance since OkHttp was designed to be shared to reuse
 * connection pools and so on.
 */
@Service
public class AuthHttpInterceptor implements Interceptor {
  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String HEADER_AUTHORIZATION_TOKEN_PREFIX = "Bearer ";

  private final TeamsAuthService teamsAuthService;
  private final RequestUrlChecker requestUrlChecker; // private dep
  private final HttpHeaderManager httpHeaderManager; // private dep

  @Autowired
  public AuthHttpInterceptor(TeamsAuthService teamsAuthService) {
    this.teamsAuthService = teamsAuthService;
    this.requestUrlChecker = new RequestUrlChecker();
    this.httpHeaderManager = new HttpHeaderManager();
  }

  /**
   * Ctor that allows configuring custom hosts for auth, mainly for testing on localhost.
   */
  public AuthHttpInterceptor(
      TeamsAuthService teamsAuthService,
      List<String> customHosts,
      boolean customHostsHttpsOnly) {
    this.teamsAuthService = teamsAuthService;
    this.requestUrlChecker = new RequestUrlChecker(customHosts, customHostsHttpsOnly);
    this.httpHeaderManager = new HttpHeaderManager();
  }

  /**
   * Intercepts the HTTP call to add the Authorization header for a conferencing user.
   */
  @Override
  public Response intercept(final Chain chain) throws IOException {
    val request = chain.request();
    val userId = httpHeaderManager.parseRequireAuthHeader(request.headers());

    // If no user, or URL is invalid for auth, proceed with original request.
    if (userId.isEmpty() || !requestUrlChecker.shouldAuthenticateRequest(request.url())) {
      return chain.proceed(request);
    }

    // Get auth info for user, proactively refreshing if needed.
    var authInfo = teamsAuthService.getAuthInfo(userId.orElseThrow());
    val authInfoWasRefreshed = authInfo.shouldRefresh();
    if (authInfoWasRefreshed) {
      authInfo = teamsAuthService.refreshAndSaveAuthInfo(userId.orElseThrow(), authInfo);
    }

    // Make request with auth added.
    var response = proceedWithAuth(chain, authInfo);

    // If 401, access token may be expired or revoked, so refresh token and try again.
    // But if we just refreshed the auth above, skip retry because the issue must be something else.
    // If the retry also fails, just let it fail and be returned to be handled elsewhere.
    if (response.code() == 401 && !authInfoWasRefreshed) {
      authInfo = teamsAuthService.refreshAndSaveAuthInfo(userId.orElseThrow(), authInfo);
      response.close(); // must close old response before making new request
      response = proceedWithAuth(chain, authInfo);
    }

    return response;
  }

  /**
   * Helper to proceed with the http chain with auth added.
   */
  private Response proceedWithAuth(Chain chain, ConferencingAuthInfo authInfo) throws IOException {
    return chain.proceed(chain
        .request()
        .newBuilder()
        // remove header that's for interceptor use only:
        .removeHeader(httpHeaderManager.getRequireAuthHeaderName())
        .addHeader(
            HEADER_AUTHORIZATION,
            HEADER_AUTHORIZATION_TOKEN_PREFIX + authInfo.accessToken().value())
        .build());
  }

  /**
   * Private class to check URLs and ensure that it's safe to authorize them.
   *
   * <p>This class is for extra security and mirrors what the SDK does by default. Basically,
   * we want to be extra sure that we only attach authorization info to URLs we trust.
   *
   * <p>This extends {@link BaseAuthenticationProvider} to reuse URL checking logic in the SDK that
   * most auth providers should use. But this class itself is not actually a full auth provider.
   */
  @Setter
  private static class RequestUrlChecker extends BaseAuthenticationProvider {
    private Set<String> customHosts;
    private boolean customHostsHttpsOnly;

    public RequestUrlChecker() {
      customHosts = Set.of();
      customHostsHttpsOnly = true;
    }

    public RequestUrlChecker(List<String> customHosts, boolean customHostsHttpsOnly) {
      this.customHosts = customHosts != null
          ? customHosts.stream().collect(Collectors.toSet())
          : Set.of();
      this.customHostsHttpsOnly = customHostsHttpsOnly;
    }

    public boolean shouldAuthenticateRequest(HttpUrl url) {
      // If we're using custom hosts (mostly for testing), auth according to custom settings.
      if (!customHosts.isEmpty()) {
        return customHosts.contains(url.host().toLowerCase(Locale.ROOT))
            && (!customHostsHttpsOnly || url.isHttps());
      }

      // Else use the default from parent class that checks for valid graph api hosts.
      return shouldAuthenticateRequestWithUrl(url.url());
    }

    @Override
    public CompletableFuture<String> getAuthorizationTokenAsync(URL requestUrl) {
      throw new UnsupportedOperationException("Not an actual auth provider");
    }
  }
}
