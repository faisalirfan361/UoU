package com.UoU.app.v1.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthMethod")
public enum AuthMethodDto {
  @JsonProperty(value = "internal") INTERNAL,
  @JsonProperty("ms-oauth-sa") MS_OAUTH_SA,
  @JsonProperty("google-oauth") GOOGLE_OAUTH,
  @JsonProperty("google-sa") GOOGLE_SA,
  @JsonProperty("conf-teams-oauth") CONF_TEAMS_OAUTH;

  /**
   * Subset of enum values that are suitable for /auth/connect endpoints.
   *
   * <p>This is mainly for @Schema annotations to control docs.
   */
  public enum AuthConnect {
    @JsonProperty("ms-oauth-sa") MS_OAUTH_SA,
    @JsonProperty("google-oauth") GOOGLE_OAUTH,
    @JsonProperty("google-sa") GOOGLE_SA,
    @JsonProperty("conf-teams-oauth") CONF_TEAMS_OAUTH,
  }

  /**
   * Subset of enum values that are suitable for direct submission of credentials to us (vs. OAuth).
   *
   * <p>This is mainly for @Schema annotations to control docs.
   */
  public enum DirectSubmission {
    @JsonProperty("google-sa") GOOGLE_SA,
  }

  /**
   * Subset of enum values that are suitable for accounts.
   */
  public enum Account {
    @JsonProperty(value = "internal") INTERNAL,
    @JsonProperty("ms-oauth-sa") MS_OAUTH_SA,
    @JsonProperty("google-oauth") GOOGLE_OAUTH,
    @JsonProperty("google-sa") GOOGLE_SA,
  }

  /**
   * Subset of enum values that are suitable for service accounts.
   */
  public enum ServiceAccount {
    @JsonProperty("ms-oauth-sa") MS_OAUTH_SA,
    @JsonProperty("google-sa") GOOGLE_SA,
  }

  /**
   * Subset of enum values that are suitable for conferencing.
   */
  public enum Conferencing {
    @JsonProperty("conf-teams-oauth") CONF_TEAMS_OAUTH,
  }
}
