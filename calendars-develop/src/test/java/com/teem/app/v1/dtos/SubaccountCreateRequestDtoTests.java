package com.UoU.app.v1.dtos;

import org.junit.jupiter.api.Test;

class SubaccountCreateRequestDtoTests {

  @Test
  void shouldReadAndWriteJson() {
    var inputJson = "{\"email\":\"test@example.com\","
        + "\"name\":\"test user\"}";

    JsonTestHelper.shouldReadAndWriteJson(inputJson, SubaccountCreateRequestDto.class);
  }
}
