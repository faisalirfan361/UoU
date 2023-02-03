package com.UoU.app.v1.unauthenticated;

import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import com.UoU.app.AuthConnectHelper;
import com.UoU.app.HtmlViewHelper;
import com.UoU.core.auth.AuthResult;
import com.UoU.core.auth.AuthService;
import com.UoU.core.auth.OauthException;
import com.UoU.core.nylas.auth.NylasAuthException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles OAuth2 provider callbacks.
 *
 * <p>This is for v1 handling, but requests will usually be forwarded from the non-versioned URLs
 * via {@link com.UoU.app.OauthEntrypointController}. This is because we have to register our
 * OAuth callback URL with providers, and we don't want it to change.
 */
@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/v1/oauth")
@Tag(name = "OAuth")
public class OauthController {
  private static final String COMMON_DESCRIPTION = "`— No authentication required`";

  public static final String ACCOUNT_ID_QUERY_PARAM = "calendarsApiAccountId";
  public static final String SERVICE_ACCOUNT_ID_QUERY_PARAM = "calendarsApiServiceAccountId";

  private final AuthService authService;
  private final HtmlViewHelper htmlViewHelper;
  private final AuthConnectHelper authConnectHelper;

  @GetMapping("/callback")
  @Operation(summary = "Handles OAuth callbacks (v1)", description = COMMON_DESCRIPTION)
  @ApiResponse(responseCode = "302", description = "Found", content = @Content())
  public ModelAndView callback(
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String state,
      @RequestParam(required = false) String error) {

    // If either code/state or error are not provided, route should not even resolve, so throw 404.
    val hasAuthParams = code != null && !code.isBlank() && state != null && !state.isBlank();
    val hasError = error != null && !error.isBlank();
    if (!(hasError || hasAuthParams)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    // If there is an error that's not the_valid_format, also throw 404 to prevent misuse.
    if (hasError && !error.matches("^[a-z_]+$")) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    // Translate provider error to friendly message:
    if (hasError) {
      switch (error) {
        case OAuth2ErrorCodes.ACCESS_DENIED:
          // This means the user canceled the oauth flow or the authZ server denied the request.
          // This is the user's fault, so return 400 and expect them to retry.
          return htmlViewHelper.error(
              HttpStatus.BAD_REQUEST,
              "Access denied. To add an account, you must grant access with the account provider.");

        case OAuth2ErrorCodes.SERVER_ERROR:
        case OAuth2ErrorCodes.TEMPORARILY_UNAVAILABLE:
          // This probably means there is something temporarily wrong with the provider.
          // This is not the user's fault, so return 500. It's not our fault either, so don't log.
          return htmlViewHelper.error(
              HttpStatus.INTERNAL_SERVER_ERROR,
              "Account provider could not complete authorization. This may indicate a temporary "
                  + "issue with the account provider.");

        default:
          // This could indicate a config issue in our code or OAuth app, so log the error. See
          // OAuth2ErrorCodes for possible issues. Error was validated above, so it's safe to log.
          return htmlViewHelper.errorWithLoggedId(
              HttpStatus.INTERNAL_SERVER_ERROR,
              "Unexpected auth error received from the account provider.",
              new OauthException("OAuth error received from provider: " + error));
      }
    }

    return htmlViewHelper.catchAndReturnInternalServerError(() -> {
      AuthResult authResult;

      try {
        authResult = authService.handleOauthCallback(code, state);
      } catch (ValidationException | NylasAuthException ex) {
        // For bad input errors, like invalid oauth sate, non-unique email, or nylas auth issues,
        // the exception message is safe for users.
        return htmlViewHelper.errorWithLoggedId(
            HttpStatus.BAD_REQUEST,
            "Invalid auth request: " + ex.getMessage(),
            ex);
      }

      return htmlViewHelper.redirect(
          authConnectHelper.getAuthSuccessRedirectUri(authResult).orElse("success"));
    });
  }

  @GetMapping("/success")
  @Operation(summary = "Renders an OAuth success page (v1)", description = COMMON_DESCRIPTION)
  @ApiResponse(
      responseCode = "200", description = "OK", content = @Content(mediaType = TEXT_HTML_VALUE))
  public ModelAndView success() {
    return htmlViewHelper.success("Your account was authorized.");
  }
}
