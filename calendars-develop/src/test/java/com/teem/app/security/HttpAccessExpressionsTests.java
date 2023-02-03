package com.UoU.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HttpAccessExpressionsTests {

  @ParameterizedTest
  @ValueSource(strings = {"null", "", " "})
  void ctor_shouldDefaultToDenyAll(String value) {
    value = value.equals("null") ? null : value;
    val expressions = new HttpAccessExpressions(
        value,
        value);

    assertThat(expressions.actuator()).isEqualTo("denyAll");
    assertThat(expressions.health()).isEqualTo("denyAll");
  }
}
