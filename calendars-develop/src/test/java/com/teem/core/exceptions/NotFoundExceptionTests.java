package com.UoU.core.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.UoU._helpers.TestData;
import lombok.val;
import org.junit.jupiter.api.Test;

class NotFoundExceptionTests {

  @Test
  void catchToOptional_shouldReturnValue() {
    val value = TestData.uuidString();
    val result = NotFoundException.catchToOptional(() -> value);

    assertThat(result).hasValue(value);
  }

  @Test
  void catchToOptional_shouldCatchNotFoundExceptionAndReturnEmpty() {
    val result = NotFoundException.catchToOptional(() -> {
      throw new NotFoundException("oops");
    });

    assertThat(result).isEmpty();
  }

  @Test
  void catchToOptional_shouldPropagateOtherExceptions() {
    val exception = new RuntimeException("oops");

    assertThatCode(() -> NotFoundException.catchToOptional(() -> {
      throw exception;
    })).isSameAs(exception);
  }
}
