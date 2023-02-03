package com.UoU.core.conferencing;

import static org.assertj.core.api.Assertions.assertThat;

import com.UoU._helpers.TestData;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConferencingAuthInfoTests {

  @ParameterizedTest
  @ValueSource(ints = {
      -300, // expires 5 minutes ago
      0, // expires right now
      300, // expires 5 minutes in the future
  })
  void shouldRefresh_shouldReturnTrueWhenExpirationIsWithin5Minutes(int expirationSecondsFromNow) {
    val info = new ConferencingAuthInfo(
        "test",
        TestData.secretString(),
        TestData.secretString(),
        Instant.now().plusSeconds(expirationSecondsFromNow));

    assertThat(info.shouldRefresh()).isTrue();
  }

  @ParameterizedTest
  @ValueSource(ints = {
      301, // expires 5 minutes, 1 second in the future
      600 // expires 10 mins in the future
  })
  void shouldRefresh_shouldReturnFalseWhenExpirationIsMoreThan5MinutesAway(
      int expirationSecondsFromNow) {
    val info = new ConferencingAuthInfo(
        "test",
        TestData.secretString(),
        TestData.secretString(),
        Instant.now().plusSeconds(expirationSecondsFromNow));

    assertThat(info.shouldRefresh()).isFalse();
  }
}
