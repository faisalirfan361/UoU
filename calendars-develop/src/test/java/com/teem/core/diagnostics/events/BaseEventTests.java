package com.UoU.core.diagnostics.events;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU.app.v1.dtos.JsonTestHelper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class BaseEventTests {

  @Test
  @SneakyThrows
  void should_serializeToJsonAndBack() {
    val event = new TestEvent("test");

    val json = JsonTestHelper.MAPPER.writeValueAsString(event);
    val deserialized = JsonTestHelper.MAPPER.readValue(json, TestEvent.class);

    assertThat(deserialized.getTime()).isEqualTo(event.getTime());
    assertThat(deserialized.getMessage()).isEqualTo(event.getMessage());
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static class TestEvent extends BaseEvent {
    public TestEvent(String message) {
      super(message);
    }
  }
}
