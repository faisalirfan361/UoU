package com.UoU.app.security;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.UoU.app.ErrorResponse;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Auth entry point that wraps BearerTokenAuthenticationEntryPoint to return a standard error body.
 *
 * <p>401 errors have to be handled here, separately from the global exception handling, because
 * spring security authentication happens further up the filter pipeline.
 */
@Component
@AllArgsConstructor
@Slf4j
class AuthenticationEntryPoint
    implements org.springframework.security.web.AuthenticationEntryPoint {

  private static final ErrorResponse ERROR_RESPONSE = new ErrorResponse(
      "Missing or invalid JWT in the Authorization header");

  private final ObjectMapper objectMapper;
  private final BearerTokenAuthenticationEntryPoint innerEntryPoint =
      new BearerTokenAuthenticationEntryPoint();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException {

    innerEntryPoint.commence(request, response, authException);
    response.setContentType(APPLICATION_JSON_VALUE);
    response.getOutputStream().write(objectMapper.writeValueAsBytes(ERROR_RESPONSE));
    log.debug("Authentication failed: {}", request.getRequestURI(), authException);
  }
}
