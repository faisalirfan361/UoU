package com.UoU.core.auth;

import java.util.Optional;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.NonNull;

/**
 * State data for OAuth, along with helpers to encode and decode while handling OAuth flows.
 */
public record OauthState(
    @NonNull AuthMethod authMethod,
    @NotNull UUID authCode
) {

  /**
   * Separator that DOES NOT need URL encoding to avoid ambiguity about when encoding is needed.
   */
  private static final String SEPARATOR = "__";

  /**
   * Encodes the state to a string.
   *
   * <p>Note that this doesn't do any URL encoding. It's expected encoding will be done as needed
   * in a specific context, like with UriComponentsBuilder or similar.
   */
  public String encode() {
    return authMethod.getValue() + SEPARATOR + authCode;
  }

  /**
   * Decodes a state string from a URL param, else returns empty if decoding fails.
   */
  public static Optional<OauthState> decode(String encoded) {
    try {
      return Optional
          .of(encoded.split(SEPARATOR))
          .filter(x -> x.length == 2)
          .flatMap(x -> AuthMethod
              .byStringValue(x[0])
              .map(method -> new OauthState(method, UUID.fromString(x[1]))));
    } catch (Exception ex) {
      return Optional.empty();
    }
  }
}
