package com.UoU.core.validation.validators;

import com.UoU.core.validation.annotations.StringContains;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;

public class StringContainsValidator implements ConstraintValidator<StringContains, String> {
  private String expectedValue;

  @Override
  @SneakyThrows
  public void initialize(StringContains annotation) {
    if (annotation.value() == null || annotation.value().isEmpty()) {
      throw new IllegalArgumentException("Invalid value for StringContains validation");
    }

    expectedValue = annotation.value();
  }

  @Override
  public boolean isValid(final String value, final ConstraintValidatorContext context) {
    return value != null && value.contains(expectedValue);
  }
}
