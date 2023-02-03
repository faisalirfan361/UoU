package com.UoU.core.nylas.tasks;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.nylas.EventQuery;
import com.UoU._helpers.TestData;
import java.time.ZoneOffset;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventQueryBuilderTests {
  private EventQuery querySpy;
  private EventQueryBuilder builder;

  @BeforeEach
  void setUp() {
    querySpy = spy(EventQuery.class);
    builder = new EventQueryBuilder(querySpy);
    reset(querySpy);
  }

  @Test
  void expandRecurringApproximatelyAroundWhen_shouldIncludeWhenTimeSpan() {
    val when = TestData.whenTimeSpan();

    builder.expandRecurringApproximatelyAroundWhen(when);

    verify(querySpy).expandRecurring(true);
    verify(querySpy).startsAfter(argThat(x -> when.startTime().isAfter(x)));
    verify(querySpy).startsBefore(argThat(x -> when.startTime().isBefore(x)));
    verify(querySpy).endsAfter(argThat(x -> when.endTime().isAfter(x)));
    verify(querySpy).endsBefore(argThat(x -> when.endTime().isBefore(x)));
  }

  @Test
  void expandRecurringApproximatelyAroundWhen_shouldIncludeWhenDate() {
    val when = TestData.whenDate();
    val start = when.date().atStartOfDay(ZoneOffset.UTC).toInstant();
    val end = when.date().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

    builder.expandRecurringApproximatelyAroundWhen(when);

    verify(querySpy).expandRecurring(true);
    verify(querySpy).startsAfter(argThat(x -> start.isAfter(x)));
    verify(querySpy).startsBefore(argThat(x -> start.isBefore(x)));
    verify(querySpy).endsAfter(argThat(x -> end.isAfter(x)));
    verify(querySpy).endsBefore(argThat(x -> end.isBefore(x)));
  }
}
