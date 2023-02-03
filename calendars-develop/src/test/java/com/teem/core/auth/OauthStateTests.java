package com.UoU.core.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.UoU._helpers.TestData;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OauthStateTests {

  @Test
  void encode_decode_shouldRoundTrip() {
    val state = new OauthState(AuthMethod.MS_OAUTH_SA, UUID.randomUUID());

    val encoded = state.encode();
    val decoded = OauthState.decode(encoded).orElseThrow();

    assertThat(decoded.authMethod()).isEqualTo(state.authMethod());
    assertThat(decoded.authCode()).isEqualTo(state.authCode());
  }

  @Test
  void decode_shouldHandleNormalAndUrlEncodedState() {
    val code = TestData.uuidString();
    val state = AuthMethod.MS_OAUTH_SA.getValue() + "__" + code;
    val urlEncodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);

    val result = OauthState.decode(state).orElseThrow();
    val urlEncodedResult = OauthState.decode(urlEncodedState).orElseThrow();

    assertThat(result).isEqualTo(urlEncodedResult);
    assertThat(result.authMethod()).isEqualTo(AuthMethod.MS_OAUTH_SA);
    assertThat(result.authCode().toString()).isEqualTo(code);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      " ",
      "__",
      " __ ",
      "no double-underscore separator in here",
      "ms-oauth-sa__not-a-uuid",
      "ms-oauth-sa__81b8aa8f-8316-414d-99bc-fa222c79a9c5__something-extra"
  })
  void decode_shouldReturnEmptyForInvalidState(String invalidState) {
    // Valid state must be: {method}__{code-uuid}
    assertThat(OauthState.decode(invalidState)).isEmpty();
  }
}
