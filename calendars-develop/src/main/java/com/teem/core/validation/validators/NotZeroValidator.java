package com.UoU.core.validation.validators;

import com.UoU.core.validation.annotations.NotZero;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotZeroValidator implements ConstraintValidator<NotZero, Object> {

  @Override
  public boolean isValid(final Object value, final ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    if (value instanceof Integer) {
      return !value.equals(0);
    }

    if (value instanceof Long) {
      return !value.equals(0L);
    }

    if (value instanceof Float) {
      return !value.equals(0F);
    }

    if (value instanceof Double) {
      return !value.equals(0D);
    }

    if (value instanceof Short) {
      return !value.equals((short) 0);
    }

    throw new IllegalArgumentException("Invalid value type for NotZero validation");
  }
}
