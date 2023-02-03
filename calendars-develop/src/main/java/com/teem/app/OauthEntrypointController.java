package com.UoU.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles OAuth2 provider callbacks by forwarding requests to the current controller version.
 *
 * <p>This endpoint is not versioned via path because we can't change the URL since it must
 * be registered with OAuth2 apps for providers. All changes to this will have to be
 * backward-compatible and forward to the correct version without changing the callback URL.
 */
@Controller
@RequestMapping("/oauth")
@AllArgsConstructor
@Slf4j
@Tag(name = "OAuth")
public class OauthEntrypointController {
  private static final String COMMON_DESCRIPTION = "`— No authentication required`\n\n"
      + "This endpoint is only an entrypoint so that we have permanent OAuth URLs. This will "
      + "forward to the current API version.";

  private final HtmlViewHelper htmlViewHelper;

  @GetMapping("/callback")
  @Operation(
      summary = "Forwards OAuth callback to the correct API version",
      description = COMMON_DESCRIPTION)
  @ApiResponse(responseCode = "302", description = "Found", content = @Content())
  public ModelAndView callback(
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String state,
      @RequestParam(required = false) String error) {
    return htmlViewHelper.forward("/v1/oauth/callback");
  }

  @GetMapping("/success")
  @Operation(
      summary = "Forwards OAuth success to the correct API version",
      description = COMMON_DESCRIPTION)
  @ApiResponse(
      responseCode = "200", description = "OK", content = @Content(mediaType = "text/html"))
  public ModelAndView success() {
    return htmlViewHelper.forward("/v1/oauth/success");
  }
}
