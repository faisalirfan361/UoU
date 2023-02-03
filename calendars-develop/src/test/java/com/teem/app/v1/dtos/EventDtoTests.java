package com.UoU.app.v1.dtos;

import org.junit.jupiter.api.Test;

public class EventDtoTests {
  @Test
  void shouldReadAndWriteJson() {
    var inputJson = "{\"id\":\"8579bd03-aea1-481b-8957-25552ef2e49a\","
        + "\"icalUid\":\"icalUid\","
        + "\"calendarId\":\"calendarId\","
        + "\"title\":\"title\","
        + "\"description\":\"description\","
        + "\"location\":\"location\","
        + "\"when\":{"
        + "\"startTime\":\"2022-01-01T13:00:00Z\","
        + "\"endTime\":\"2022-01-01T14:00:00Z\","
        + "\"type\":\"timespan\"},"
        + "\"recurrenceInstance\":{"
        + "\"masterId\":\"4f2f26c4-7d11-499f-9903-4d4231f25ab9\","
        + "\"isOverride\":true},"
        + "\"status\":\"confirmed\","
        + "\"isBusy\":true,"
        + "\"isReadOnly\":false,"
        + "\"checkinAt\":\"2021-01-01T10:35:05Z\","
        + "\"checkoutAt\":\"2021-01-01T10:35:06Z\","
        + "\"owner\":{\"name\":\"owner\",\"email\":\"test@test.com\"},"
        + "\"participants\":["
        + "{\"name\":\"Participant 1\","
        + "\"email\":\"p1@test.com\","
        + "\"status\":\"yes\","
        + "\"comment\":\"comment\"},"
        + "{\"name\":\"Participant 2\","
        + "\"email\":\"p2@test.com\","
        + "\"status\":\"noreply\","
        + "\"comment\":\"comment\"}],"
        + "\"createdAt\":\"2021-01-01T10:35:05Z\","
        + "\"createdFrom\":\"api:mobile\","
        + "\"updatedAt\":\"2021-01-01T10:35:06Z\","
        + "\"updatedFrom\":\"provider\"}";

    JsonTestHelper.shouldReadAndWriteJson(inputJson, EventDto.class);
  }
}
