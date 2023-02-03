package com.UoU.core.nylas;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class RecurrenceInfoTests {

  /**
   * Tests that an event is considered an override instance based on original_start_time existing.
   */
  @Test
  void shouldBeOverrideWhenEventHasOriginalStartTime() {
    var hasOriginalStartTime = new RecurrenceInfo(createRecurrenceInstance(true));
    var noOriginalStartTime = new RecurrenceInfo(createRecurrenceInstance(false));

    var id = ZoneId.systemDefault();

    assertThat(hasOriginalStartTime)
        .returns(true, x -> x.isInstance())
        .returns(true, x -> x.isOverrideInstance())
        .returns(false, x -> x.isNonOverrideInstance());

    assertThat(noOriginalStartTime)
        .returns(true, x -> x.isInstance())
        .returns(false, x -> x.isOverrideInstance())
        .returns(true, x -> x.isNonOverrideInstance());
  }

  @Test
  void toString_shouldWork() {
    assertThat(new RecurrenceInfo(createMaster()).toString()).isNotBlank();
    assertThat(new RecurrenceInfo(createRecurrenceInstance(true)).toString()).isNotBlank();
    assertThat(new RecurrenceInfo(createRecurrenceInstance(false)).toString()).isNotBlank();
  }

  private static com.nylas.Event createMaster() {
    var event = spy(com.nylas.Event.class);
    when(event.getMasterEventId()).thenReturn(null);
    return event;
  }

  private static com.nylas.Event createRecurrenceInstance(boolean hasOriginalStartTime) {
    var event = spy(com.nylas.Event.class);
    when(event.getMasterEventId()).thenReturn("abc123");
    when(event.getOriginalStartTime()).thenReturn(hasOriginalStartTime ? Instant.now() : null);
    return event;
  }
}
