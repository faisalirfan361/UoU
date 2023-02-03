package com.UoU.core.validation;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.TestData;
import com.UoU.core._helpers.ConstraintViolationFactory;
import java.util.Optional;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotBlank;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Test;

class ViolationFactoryTests {
  private static final ViolationFactory FACTORY = new ViolationFactory();

  @Test
  void create_withViolation_shouldUseFullPathForNormalObject() {
    val value = new TestClass("");
    val violation = ConstraintViolationFactory.createSingleViolation(value);

    val result = FACTORY.create(violation);

    assertThat(result.field()).isEqualTo("prop");
  }

  @Test
  void create_withViolation_shouldUseFullPathWhenViolationFieldNameProviderReturnsEmpty() {
    val value = new TestProviderClass("", null);
    val violation = ConstraintViolationFactory.createSingleViolation(value);

    val result = FACTORY.create(violation);

    assertThat(result.field()).isEqualTo("prop");
  }

  @Test
  void create_withViolation_shouldGetFieldNameFromVioliationFieldNameProvider() {
    val value = new TestProviderClass("", TestData.uuidString());
    val violation = ConstraintViolationFactory.createSingleViolation(value);

    val result = FACTORY.create(violation);

    assertThat(result.field()).isEqualTo(value.getViolationFieldName());
  }

  @Value
  private static class TestClass {
    @NotBlank String prop;
  }

  @Value
  private static class TestProviderClass implements ViolationFieldNameProvider {
    @NotBlank String prop;
    String violationFieldName;

    @Override
    public Optional<String> getViolationFieldName(ConstraintViolation<?> violation) {
      return Optional.ofNullable(violationFieldName);
    }
  }
}
