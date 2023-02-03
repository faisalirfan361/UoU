package com.UoU.core.events;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Set;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DataSourceTests {

  @ParameterizedTest
  @ValueSource(strings = {
      "mobile",
      "50 chars, which is the max........................"
  })
  void validation_shouldPass(String name) {
    assertThatValidationPasses(new DataSource(name));
  }

  @Test
  void validation_shouldFailOnNull() {
    assertThatValidationFails(Set.of("value"), new DataSource(null));
  }

  @ParameterizedTest
  @ValueSource(strings = { "", " ", "51 chars, max is 50................................" })
  void validation_shouldFailOnBadString(String name) {
    assertThatValidationFails(Set.of("value"), new DataSource(name));
  }

  @Test
  void fromApi_shouldTrimAndIncludePrefix() {
    val result = DataSource.fromApi("  test  ").value();
    assertThat(result).isEqualTo("api:test");
  }

  @Test
  void fromApi_shouldUseDefaultWhenValueIsBlank() {
    val result = DataSource.fromApi("  ").value();
    assertThat(result).isEqualTo("api");
  }
}
