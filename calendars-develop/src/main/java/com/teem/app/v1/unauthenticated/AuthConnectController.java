package com.UoU.app.v1.unauthenticated;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.UoU.app.AuthConnectHelper;
import com.UoU.app.HtmlViewHelper;
import com.UoU.app.v1.dtos.AuthMethodDto;
import com.UoU.app.v1.dtos.IdResponse;
import com.UoU.core.Fluent;
import com.UoU.core.auth.AuthCode;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.auth.AuthResult;
import com.UoU.core.auth.AuthService;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.nylas.auth.NylasAuthException;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Allows an end-user to authorize/connect a calendar account.
 *
 * <p>This is protected by requiring a valid auth code, which must be generated ahead of time by
 * an outside app that has API access and permission to generate auth codes. Each auth code is a
 * short-lived code tied to a specific org. See {@link com.UoU.app.v1.AuthCodeController}.
 */
@Controller
@AllArgsConstructor
@RequestMapping("/v1/auth/connect")
@Slf4j
@Tag(name = "Auth",
    externalDocs = @ExternalDocumentation(
        description = "See more about accounts and service accounts",
        url = "/docs/v1.html#accounts"))
public class AuthConnectController {

  private static final String AUTH_DATA_JSON_KEY = "json";

  private static final String COMMON_DESCRIPTION = """
      `— No authentication required`

      This is for end users to authorize a calendar or conferencing account into a specific
      organization.

      An auth code must have been previously generated via **/v1/auth/codes**. The auth code
      determines where the user will be redirected after authorization.
      """;

  private final AuthService authService;
  private final HtmlViewHelper htmlViewHelper;
  private final AuthConnectHelper authConnectHelper;
  private final ObjectMapper objectMapper;

  /**
   * Redirects to /calendar/{code} for backward compatability, since there used to be only one ui.
   */
  @GetMapping("/{code}")
  @Operation(hidden = true)
  public ModelAndView connectDefaultUi(@PathVariable String code) {
    return htmlViewHelper.redirect("calendar/" + code);
  }

  @GetMapping("/calendar/{code}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Authorize a calendar account with a UI that allows selecting a method",
      description = COMMON_DESCRIPTION)
  @ApiResponse(
      responseCode = "200", description = "OK", content = @Content(mediaType = TEXT_HTML_VALUE))
  public ModelAndView connectCalendarUi(@PathVariable String code) {
    return htmlViewHelper.catchAndReturnInternalServerError(() ->
        withValidAuthCode(code, CalendarAuthView::create));
  }


  @GetMapping("/conferencing/{code}")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Authorize a conferencing account with a UI that allows selecting a method",
      description = COMMON_DESCRIPTION)
  @ApiResponse(
      responseCode = "200", description = "OK", content = @Content(mediaType = TEXT_HTML_VALUE))
  public ModelAndView connectConferencingUi(@PathVariable String code) {
    return htmlViewHelper.catchAndReturnInternalServerError(() ->
        withValidAuthCode(code, ConferencingAuthView::create));
  }

  /**
   * Handles auth for a specific auth method.
   *
   * <p>This is mainly used for OAuth methods since it's redirect-based and uses a GET. For methods
   * that require the user to submit data, a form is rendered to collect the data instead.
   *
   * <p>On success, this results in a 302 redirect if the auth code specified a redirect url, else
   * a success page is rendered.
   */
  @GetMapping("/{method}/{code}")
  @Operation(
      summary = "Authorize a calendar or conferencing account using a specific method",
      description = COMMON_DESCRIPTION + "\n\n" + """
          For most OAuth methods, this redirects to the OAuth provider to start the OAuth flow.
          For non-OAuth and 2-legged OAuth methods, a form will be rendered to collect the required
          auth data from the user; see the POST endpoint if you want to use your own form to collect
          auth data.
          """)
  @ApiResponses({
      @ApiResponse(
          responseCode = "200", description = "OK",
          content = @Content(mediaType = TEXT_HTML_VALUE)),
      @ApiResponse(
          responseCode = "302", description = "Found", content = @Content()),
  })
  public ModelAndView connect(
      @PathVariable @Schema(implementation = AuthMethodDto.AuthConnect.class) String method,
      @PathVariable String code) {

    return htmlViewHelper.catchAndReturnInternalServerError(() -> {
      val methodEnum = AuthMethod.byStringValue(method);
      if (methodEnum.filter(x -> x.getFlow() != AuthMethod.Flow.None).isEmpty()) {
        return htmlViewHelper.error(HttpStatus.NOT_FOUND, "Invalid auth method");
      }

      return withValidAuthCode(code, validAuthCode -> switch (methodEnum.orElseThrow().getFlow()) {
        // For OAuth auth code flow, redirect to OAuth provider and wait for callback:
        case OauthAuthorizationCode -> htmlViewHelper.redirect(
            authService.getOauthRedirectUrl(methodEnum.orElseThrow(), validAuthCode));

        // For direct submission flow, render a form to collect the data from the user:
        case DirectSubmission -> CalendarAuthView.create(validAuthCode, methodEnum.orElseThrow());

        // None case should be checked above, so this should never happen, but in case of bugs:
        default -> throw new IllegalArgumentException("Invalid auth method");
      });
    });
  }

  /**
   * Handles x-www-form-urlencoded submission of auth data for methods that support it.
   *
   * <p>On success, this results in a 302 redirect if the auth code specified a redirect url, else a
   * success is rendered. This redirect works just like {@link #connect(String, String)}.
   *
   * <p>Technically, for HTTP 1.1, this should return a 303 redirect, but this returns 302 for a few
   * reasons: 303 doesn't work for HTTP 1.0, most clients treat 302/303 the same anyway, and there
   * doesn't seem to be an easy way to turn off HTTP 1.0 compatibility in the spring view resolvers
   * without configuring them all from scratch so we'll just stick to the spring default behavior.
   *
   * <p>JSON auth data is also supported via {@link #connectJson(String, String, Map)}. But this
   * method has all the swagger annotations to make springdoc work correctly.
   */
  @PostMapping(
      value = "/{method}/{code}",
      consumes = APPLICATION_FORM_URLENCODED_VALUE,
      produces = TEXT_HTML_VALUE)
  @Operation(
      summary = "Submit auth data to authorize a calendar account using a specific method",
      description = COMMON_DESCRIPTION + "\n\n" + """
          This POST endpoint only works for non-OAuth and 2-legged OAuth methods where auth data
          needs to be collected directly from a user.

          **google-sa:** For the Google Service Account method (google-sa), a Google service account
          JSON key must be downloaded from the Google Cloud console and submitted. The JSON key can
          can be submitted one of two ways: a) using **application/x-www-form-urlencoded** with
          a key named "json" that contains the Google JSON, or b) using **application/json** with
          the request body containing the Google JSON.

          If the request content-type is **application/x-www-form-urlencoded**, a 302 redirect or
          **text/html** success response will be returned, depending on the auth code. If the
          request content-type is **application/json**, the response type will always be
          **application/json** and will contain the resulting account or service account id.
          """)
  @ApiResponses({
      @ApiResponse(
          responseCode = "200", description = "OK",
          content = @Content(mediaType = TEXT_HTML_VALUE)),
      @ApiResponse(
          responseCode = "302", description = "See Other", content = @Content()),
  })
  public ModelAndView connectForm(
      @PathVariable @Schema(implementation = AuthMethodDto.DirectSubmission.class)
      String method,
      @PathVariable String code,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          content = @Content(
              schemaProperties = @SchemaProperty(name = "json",
                  schema = @Schema(implementation = String.class))))
      @RequestBody MultiValueMap<String, Object> data) {

    return htmlViewHelper.catchAndReturnInternalServerError(() -> {
      val methodEnum = AuthMethod.byStringValue(method);
      if (methodEnum.isEmpty()) {
        return htmlViewHelper.error(HttpStatus.NOT_FOUND, "Invalid auth method");
      }

      val finalData = Optional
          .of(data.toSingleValueMap())
          .flatMap(map -> Optional
              .of(map)
              .filter(x -> x.size() == 1)
              .map(x -> (String) x.get(AUTH_DATA_JSON_KEY))
              .flatMap(this::tryReadJson))
          .orElse(Map.of());

      AuthResult authResult;
      try {
        authResult = authService.handleDirectionSubmissionAuth(
            methodEnum.orElseThrow(), code, finalData);
      } catch (ValidationException | NylasAuthException ex) {
        // For bad input errors, like ones that result in nylas auth issues, the exception message
        // is safe for users.
        return htmlViewHelper.error(
            HttpStatus.BAD_REQUEST, "Invalid auth request: " + ex.getMessage());
      }

      return htmlViewHelper.redirect(
          authConnectHelper.getAuthSuccessRedirectUri(authResult).orElse("../success"));
    });
  }

  /**
   * Handles application/json submission of auth data for methods that support it.
   *
   * <p>302 redirect doesn't make sense for a JSON submission, so this always returns the authed id.
   *
   * <p>Form auth data is also supported via {@link #connectForm(String, String, MultiValueMap)}.
   */
  @PostMapping(
      value = "/{method}/{code}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public IdResponse<String> connectJson(
      @PathVariable @Schema(implementation = AuthMethodDto.DirectSubmission.class) String method,
      @PathVariable String code,
      @RequestBody Map<String, Object> data) {

    val methodEnum = AuthMethod.byStringValue(method);
    if (methodEnum.isEmpty()) {
      throw NotFoundException.ofName("Auth method");
    }

    AuthResult authResult;
    try {
      authResult = authService.handleDirectionSubmissionAuth(methodEnum.orElseThrow(), code, data);
    } catch (NylasAuthException ex) {
      // Treat nylas auth issues as validation exceptions so user can see the exception message
      // and correct the input.
      throw new ValidationException(ex.getMessage());
    }

    return new IdResponse<>(authResult.id());
  }

  @GetMapping("/success")
  @Operation(summary = "Renders an auth connect success page")
  @ApiResponse(
      responseCode = "200", description = "OK", content = @Content(mediaType = TEXT_HTML_VALUE))
  public ModelAndView success() {
    return htmlViewHelper.success("Your account info was submitted.");
  }

  private Optional<Map<String, Object>> tryReadJson(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<>() {
      });
    } catch (JsonProcessingException ex) {
      return Optional.empty();
    }
  }

  /**
   * Returns the supplied ModelAndView if the auth code is valid else returns an error view.
   */
  private ModelAndView withValidAuthCode(
      String code,
      Function<AuthCode, ModelAndView> modelAndViewFunc) {

    return authService
        .tryGetValidAuthCode(code)
        .map(modelAndViewFunc)
        .orElseGet(() -> htmlViewHelper.error(HttpStatus.NOT_FOUND, "Invalid auth code"));
  }

  /**
   * Helper class to build a map of properties needed for the calendar auth view.
   */
  private static class CalendarAuthView {
    public static ModelAndView create(AuthCode code) {
      return create(code, null);
    }

    public static ModelAndView create(AuthCode code, @Nullable AuthMethod method) {
      return Fluent
          .of(new HashMap<String, Object>())
          .also(x -> x.put("calendar-auth", true))
          .also(x -> x.put("orgId", code.orgId().value()))
          .also(x -> x.put("code", code.code()))
          .ifThenAlso(method != null, x -> x.put("method", method.getValue()))
          .ifThenAlso(method != null, x -> x.put("method-" + method.getValue(), true))
          .map(x -> new ModelAndView("auth", x))
          .get();
    }
  }

  /**
   * Helper class to build a map of properties needed for the conferencing auth view.
   */
  private static class ConferencingAuthView {
    public static ModelAndView create(AuthCode code) {
      return Fluent
          .of(new HashMap<String, Object>())
          .also(x -> x.put("conferencing-auth", true))
          .also(x -> x.put("orgId", code.orgId().value()))
          .also(x -> x.put("code", code.code()))
          .map(x -> new ModelAndView("auth", x))
          .get();
    }
  }
}
