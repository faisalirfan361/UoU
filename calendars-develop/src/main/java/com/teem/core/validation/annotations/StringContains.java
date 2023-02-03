package com.UoU.core.validation.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.UoU.core.validation.validators.StringContainsValidator;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Ensures a string contains the expected value, which also means the string cannot be null.
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = StringContainsValidator.class)
public @interface StringContains {
  String message() default "must contain '{value}'";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
  String value();
}
