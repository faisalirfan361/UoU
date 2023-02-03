package com.UoU.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

import org.junit.jupiter.api.Test;

class FluentTests {

  @Test
  void map_shouldReturnResultOfMapper() {
    var mappedResult = new Object();
    var result = Fluent
        .of("hi")
        .map(x -> mappedResult);

    assertThat(result.get()).isSameAs(mappedResult);
  }

  @Test
  void ifThenAlso_shouldCheckCondition() {
    var fluent = Fluent.of("hi");

    fluent.ifThenAlso(x -> false, x -> fail("Expected not to run"));
    assertThatCode(() -> fluent.ifThenAlso(x -> true, x -> {
      throw new RuntimeException("ran");
    })).hasMessage("ran");
  }

  @Test
  void ifThenThrow_shouldCheckConditionAndThrowSuppliedException() {
    var fluent = Fluent.of("hi");

    assertThatCode(() -> fluent.ifThenThrow(x -> false, () -> new RuntimeException("ran")))
        .doesNotThrowAnyException();
    assertThatCode(() -> fluent.ifThenThrow(x -> true, () -> new RuntimeException("ran")))
        .hasMessage("ran");
  }
}
