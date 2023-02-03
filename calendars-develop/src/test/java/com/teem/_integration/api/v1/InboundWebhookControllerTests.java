package com.UoU._integration.api.v1;

import static org.hamcrest.Matchers.equalTo;

import com.UoU._integration.api.BaseApiIntegrationTest;
import com.UoU.app.security.HmacUtil;
import com.UoU.app.v1.dtos.NylasActionDto;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

public class InboundWebhookControllerTests extends BaseApiIntegrationTest {
  @Getter private final String basePath = "/v1/inbound-webhooks";

  @Autowired
  HmacUtil hmacUtil;

  @Test
  void nylasWebhookVerification_shouldWork() {
    val challengeCode = "challenge_code";
    restAssured()
        .queryParam("challenge", challengeCode)
        .get("/nylas")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(equalTo(challengeCode));
  }

  @SneakyThrows
  @ParameterizedTest
  @EnumSource(NylasActionDto.class)
  void nylasWebhook_shouldWork(NylasActionDto action) {
    val body = createBody(action.getValue());
    val hmac = createHmac(body);

    restAssured()
        .header("X-Nylas-Signature", hmac)
        .body(body.toString())
        .post("/nylas")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @SneakyThrows
  @Test
  void invalidNylasWebhookType_shouldReturnBadRequest() {
    val body = createBody("invalid.webhook");
    val hmac = createHmac(body);

    restAssured()
        .header("X-Nylas-Signature", hmac)
        .body(body.toString())
        .post("/nylas")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @SneakyThrows
  @Test
  void invalidHmac_shouldReturnBadRequest() {
    val hmac = createHmac(new JSONObject().put("bad", "value"));

    restAssured()
        .header("X-Nylas-Signature", hmac)
        .body("")
        .post("/nylas")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @SneakyThrows
  private String createHmac(JSONObject json) {
    return hmacUtil.createHmac(json.toString());
  }

  @SneakyThrows
  private static JSONObject createBody(String webhookType) {
    val json = new JSONObject();
    val deltas = new JSONArray();
    json.put("deltas", deltas);

    val objectData = new JSONObject();
    objectData
        .put("namespace_id", "testNamespaceId")
        .put("account_id", "testAccountId")
        .put("object", webhookType.split("\\.")[0])
        .put("attributes", JSONObject.NULL)
        .put("id", "testId")
        .put("metadata", JSONObject.NULL);

    val delta = new JSONObject()
        .put("object_data", objectData)
        .put("date", 1234567890)
        .put("object", webhookType.split("\\.")[0])
        .put("type", webhookType);

    deltas.put(delta);
    return json;
  }
}
