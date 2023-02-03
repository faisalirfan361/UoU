package com.UoU.app.v1.dtos;

import org.junit.jupiter.api.Test;

class CalendarDtoTests {

  @Test
  void shouldReadAndWriteJson() {
    var inputJson = "{\"id\":\"id\","
        + "\"accountId\":\"accountId\","
        + "\"name\":\"name\","
        + "\"timezone\":\"America/Denver\","
        + "\"createdAt\":\"2021-01-01T10:35:05Z\","
        + "\"updatedAt\":\"2021-01-01T10:35:06Z\"}";
    JsonTestHelper.shouldReadAndWriteJson(inputJson, CalendarDto.class);
  }
}
