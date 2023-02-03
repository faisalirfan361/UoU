package com.UoU.app.v1.dtos;

import org.junit.jupiter.api.Test;

class AvailabilityRequestDtoTests {

  @Test
  void shouldReadAndWriteJson() {
    var inputJson = "{\"calendarIds\":[\"test-id\"],"
        + "\"timeSpan\":{\"start\":\"2022-03-01T01:02:03Z\","
        + "\"end\":\"2022-03-02T01:02:03Z\"}}";
    JsonTestHelper.shouldReadAndWriteJson(inputJson, AvailabilityRequestDto.class);
  }
}
