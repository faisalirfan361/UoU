package com.UoU.core.validation.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.UoU.core.validation.validators.TimeZoneValidator;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Ensures a string is a valid IANA timezone name like "America/Denver".
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = TimeZoneValidator.class)
public @interface TimeZone {
  String message() default "must be a valid timezone";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
