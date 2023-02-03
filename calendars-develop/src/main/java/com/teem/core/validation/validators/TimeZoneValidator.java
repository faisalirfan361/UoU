package com.UoU.core.validation.validators;

import com.UoU.core.validation.annotations.TimeZone;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class TimeZoneValidator implements ConstraintValidator<TimeZone, String> {

  @Override
  public boolean isValid(final String value, final ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    try {
      // Timezone is valid if Spring util can parse it, which is mostly just TimeZone.getTimeZone().
      StringUtils.parseTimeZoneString(value);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}
