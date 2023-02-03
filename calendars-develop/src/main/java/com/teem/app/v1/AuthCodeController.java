package com.UoU.app.v1;

import com.UoU.app.security.Authorize;
import com.UoU.app.security.PrincipalProvider;
import com.UoU.app.v1.dtos.AuthCodeCreateRequestDto;
import com.UoU.app.v1.dtos.AuthCodeResponseDto;
import com.UoU.app.v1.mapping.AuthMapper;
import com.UoU.app.v1.unauthenticated.OauthController;
import com.UoU.core.DataConfig;
import com.UoU.core.auth.AuthService;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Authorize.AccountsWrite // default, but override on each method for clarity
@RestController
@AllArgsConstructor
@RequestMapping("/v1/auth/codes")
@Tag(name = "Auth",
    externalDocs = @ExternalDocumentation(
    description = "See more about accounts and service accounts",
    url = "/docs/v1.html#accounts"))
public class AuthCodeController {
  private final AuthService authService;
  private final AuthMapper authMapper;
  private final PrincipalProvider principalProvider;

  @Authorize.AccountsWrite
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Generate a one-time code that allows calendar account authorization",
      description = Authorize.AccountsWrite.DESCRIPTION
        + "This generates a short-lived, one-time code that allows an end user to authorize a "
        + "calendar account into your organization. After generating the code, redirect the user "
        + "to either **/v1/auth/connect/{code}** or **/v1/auth/connect/{method}/{code}** so they "
        + "can complete the authorization flow.\n\n"
        + "If you provide a **redirectUri**, the user who completes account authorization will "
        + "be redirected to the URI on success. A query parameter of either **"
        + OauthController.ACCOUNT_ID_QUERY_PARAM + "={id}** (for normal accounts) or **"
        + OauthController.SERVICE_ACCOUNT_ID_QUERY_PARAM + "={id}** (for service accounts) will "
        + "be added to the URI so that the external application will have the id to use for "
        + "subsequent API calls. If you do not provide a **redirectUri**, the user will be "
        + "shown a generic success page after authorization.\n\n"
        + "Codes expire after **" + DataConfig.Auth.AUTH_CODE_EXPIRATION_MINUTES + " minutes**.")
  public AuthCodeResponseDto generateCode(
      @RequestBody(required = false) AuthCodeCreateRequestDto request) {

    var code = authService.createAuthCode(authMapper.toModel(
        request,
        principalProvider.current().orgId(),
        DataConfig.Auth.AUTH_CODE_EXPIRATION_MINUTES));

    return new AuthCodeResponseDto(code.toString());
  }
}
