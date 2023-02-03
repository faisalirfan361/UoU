package com.UoU.infra.oauth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.UoU._helpers.TestData;
import com.UoU.core.auth.OauthConfig;
import com.UoU.core.auth.OauthException;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class OauthClientTests {
  private static final RestTemplate REST_TEMPLATE = new RestTemplate();
  private static final MockRestServiceServer MOCK_SERVER = MockRestServiceServer
      .createServer(REST_TEMPLATE);
  private static final String TOKEN_URI = "https://localhost/test/token";
  private static final String TOKEN_JSON = "{\"id_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
      + "eyJlbWFpbCI6InhAZXhhbXBsZS5jb20iLCJuYW1lIjoidGVzdCJ9."
      + "jkcpIhhTzBlWwaKIvhLgxAExd_Q0oBw_j7XEM2AeShI\", "
      + "\"refresh_token\": \"refresh\", "
      + "\"access_token\": \"access\", "
      + "\"expires_in\": 999}";
  private static final OauthConfig OAUTH_CONFIG = TestData.oauthConfig();
  private static final OauthClient CLIENT = new OauthClient(REST_TEMPLATE, OAUTH_CONFIG);

  @BeforeEach
  void setUp() {
    MOCK_SERVER.reset();
  }

  @Test
  @SneakyThrows
  void exchangeAuthorizationCode_shouldPostAsFormUrlEncoded() {
    val extraParamInput = "test param value";
    val extraParamEncoded = "test+param+value";

    MOCK_SERVER.expect(ExpectedCount.once(), requestTo(TOKEN_URI))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(x -> assertThat(((MockClientHttpRequest) x).getBodyAsString())
            .contains("=" + extraParamEncoded))
        .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TOKEN_JSON));

    val result = CLIENT.exchangeAuthorizationCode(
        TOKEN_URI,
        OAUTH_CONFIG.google(),
        "code",
        Map.of("param", extraParamInput));

    assertThat(result).isNotNull();
    MOCK_SERVER.verify();
  }

  @ParameterizedTest
  @ValueSource(ints = {400, 401, 403, 500, 502})
  void exchangeAuthorizationCode_shouldThrowOauthExceptionForErrorStatusCodes(int status) {
    MOCK_SERVER.expect(ExpectedCount.once(), requestTo(TOKEN_URI))
        .andRespond(withStatus(HttpStatus.valueOf(status)));

    assertThatCode(() -> CLIENT.exchangeAuthorizationCode(TOKEN_URI, OAUTH_CONFIG.google(), "code"))
        .isInstanceOf(OauthException.class)
        .hasMessageContaining("status " + status);

    MOCK_SERVER.verify();
  }
}
