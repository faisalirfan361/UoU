package com.UoU.app.v1.dtos.nylas;

import com.UoU.app.v1.dtos.JsonTestHelper;
import lombok.val;
import org.junit.jupiter.api.Test;

public class NotificationDtoTests {

  @Test
  void shouldReadAndWriteJson() {
    val inputJson = "{\"deltas\": [{"
        + "\"object_data\": {"
        + "\"namespace_id\": \"namespace_id\","
        + "\"account_id\": \"account_id\","
        + "\"object\": \"event\","
        + "\"attributes\": null,"
        + "\"id\": \"id\","
        + "\"metadata\": null},"
        + "\"date\": 1644246755,"
        + "\"object\": \"event\","
        + "\"type\": \"" + "event.created" + "\"}]}";

    val expectedJson = "{\"deltas\":[{"
        + "\"object_data\":{"
        + "\"account_id\":\"account_id\","
        + "\"object\":\"event\","
        + "\"id\":\"id\"},"
        + "\"date\":1644246755,"
        + "\"object\":\"event\","
        + "\"type\":\"" + "event.created" + "\"}]}";

    JsonTestHelper.shouldReadAndWriteJson(inputJson, expectedJson, NotificationDto.class);
  }
}
