package com.UoU.core.validation.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.UoU.core.validation.validators.NotZeroValidator;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Ensures a numeric value is not zero (null is allowed because it's not zero).
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = NotZeroValidator.class)
public @interface NotZero {
  String message() default "must not be 0";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
