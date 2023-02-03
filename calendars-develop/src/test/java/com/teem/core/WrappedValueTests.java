package com.UoU.core;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU.core._helpers.ConstraintViolationFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.val;
import org.junit.jupiter.api.Test;

class WrappedValueTests {

  /**
   * Tests that when class is not wrapped, field name is lowercased class name rather than "value".
   */
  @Test
  void getViolationFieldName_shouldReturnLowercasedClassNameWhenNotWrapped() {
    val value = new TestValue("");
    val violation = ConstraintViolationFactory.createSingleViolation(value);

    val result = value.getViolationFieldName(violation);

    assertThat(result).hasValue("testValue");
  }

  /**
   * Tests that the final .value path portion is removed to make more sense.
   */
  @Test
  void getViolationFieldName_shouldOmitValueFromPathWhenWrapped() {
    val value = new TestValue("");
    val wrapper = new TestWrapper(value);
    val violation = ConstraintViolationFactory.createSingleViolation(wrapper);

    val result = value.getViolationFieldName(violation);

    assertThat(result).hasValue("testValue");
  }

  /**
   * Tests that the final .value path portion is removed when wrapped multiple levels deep.
   */
  @Test
  void getViolationFieldName_shouldOmitValueFromPathWhenWrappedDeeper() {
    val value = new TestValue("");
    val wrapper = new TestWrapper(value);
    val wrapperWrapper = new TestWrapperWrapper(wrapper);
    val violation = ConstraintViolationFactory.createSingleViolation(wrapperWrapper);

    val result = value.getViolationFieldName(violation);

    assertThat(result).hasValue("testWrapper.testValue");
  }

  record TestValue(@NotBlank String value) implements WrappedValue<String> {}

  record TestWrapper(@Valid TestValue testValue) {}

  record TestWrapperWrapper(@Valid TestWrapper testWrapper) {}
}
