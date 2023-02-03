package com.UoU.core;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.UoU._helpers.TestData;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TimeSpanTests {
  private static final Instant NOW = Instant.now();

  @Test
  void validation_shouldPass() {
    var timeSpan = new TimeSpan(Instant.now(), Instant.now().plusSeconds(1));
    assertThatValidationPasses(timeSpan);
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, TimeSpan timeSpan) {
    assertThatValidationFails(invalidProps, timeSpan);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("start", "end"),
            new TimeSpan((Instant) null, (Instant) null)),
        Arguments.of(
            Set.of("end"),
            new TimeSpan(NOW, NOW)),
        Arguments.of(
            Set.of("end"),
            new TimeSpan(NOW, NOW.minusSeconds(1))),
        Arguments.of(
            Set.of("end"),
            new TimeSpan(Instant.now(), Instant.now().plusSeconds(60))
                .withMaxDuration(Duration.ofSeconds(59)))
    );
  }

  @Test
  void maxDurationDescription_shouldReturnUserFriendlyText() {
    val timeSpan = new TimeSpan(NOW, NOW.plusSeconds(1));

    assertThat(timeSpan.withMaxDuration(Duration.ofDays(1)).maxDurationDescription())
        .isEqualTo("1 day");
    assertThat(timeSpan.withMaxDuration(Duration.ofDays(2)).maxDurationDescription())
        .isEqualTo("2 days");
    assertThat(timeSpan.withMaxDuration(Duration.ofHours(2)).maxDurationDescription())
        .isEqualTo("2 hours");
    assertThat(timeSpan.withMaxDuration(Duration.ofMinutes(2)).maxDurationDescription())
        .isEqualTo("2 minutes");
    assertThat(timeSpan.withMaxDuration(Duration.ofSeconds(2)).maxDurationDescription())
        .isEqualTo("2 seconds");
    assertThat(timeSpan.withMaxDuration(Duration.ofDays(2).plusMinutes(3)).maxDurationDescription())
        .isEqualTo("2 days, 3 minutes");
    assertThat(timeSpan
        .withMaxDuration(Duration.ofHours(2).plusMinutes(3)).maxDurationDescription())
        .isEqualTo("2 hours, 3 minutes");
  }

  @Test
  void equals_shouldUseInstantValues() {
    val start = "2022-01-01T01:02:03Z";
    val end = "2022-01-02T01:02:03Z";

    assertThat(new TimeSpan(Instant.parse(start), Instant.parse(end)))
        .isEqualTo(new TimeSpan(Instant.parse(start), Instant.parse(end)));
  }

  @Test
  void contains_shouldReturnWhetherInstantIsWithin() {
    val timeSpan = TestData.timeSpan();

    // contains() == true
    Stream.of(
        timeSpan.start(), // start is inclusive
        timeSpan.start().plusSeconds(1),
        timeSpan.end().minusSeconds(1)
    ).forEach(x -> assertThat(timeSpan.contains(x)).isTrue());

    // contains == false
    Stream.of(
        timeSpan.start().minusSeconds(1),
        timeSpan.end(), // end is exclusive
        timeSpan.end().plusSeconds(1)
    ).forEach(x -> assertThat(timeSpan.contains(x)).isFalse());
  }

  @Test
  void contains_shouldReturnWhetherOtherTimeSpanIsWithin() {
    val timeSpan = TestData.timeSpan();

    // contains() == true
    Stream.of(
        timeSpan,
        new TimeSpan(timeSpan.start().plusSeconds(1), timeSpan.end()),
        new TimeSpan(timeSpan.start(), timeSpan.end().minusSeconds(1)),
        new TimeSpan(timeSpan.start().plusSeconds(1), timeSpan.end().minusSeconds(1))
    ).forEach(x -> assertThat(timeSpan.contains(x)).isTrue());

    // contains() == false
    Stream.of(
        new TimeSpan(timeSpan.start().minusSeconds(1), timeSpan.end()),
        new TimeSpan(timeSpan.start(), timeSpan.end().plusSeconds(1)),
        new TimeSpan(timeSpan.start().minusSeconds(1), timeSpan.end().plusSeconds(1))
    ).forEach(x -> assertThat(timeSpan.contains(x)).isFalse());
  }

  @Test
  void isBefore_shouldReturnTrueIfEndGteInstant() {
    val timeSpan = TestData.timeSpan();

    assertThat(timeSpan.isBefore(timeSpan.end())).isTrue(); // end is exclusive
    assertThat(timeSpan.isBefore(timeSpan.end().plusSeconds(1))).isTrue();

    assertThat(timeSpan.isBefore(timeSpan.end().minusSeconds(1))).isFalse();
  }
}
