package com.UoU.core.validation.annotations;

import com.UoU.core.validation.validators.CustomValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * Adds custom validation with a custom {@link Validator} function.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomValidator.class)
public @interface Custom {
  String message() default "must be valid"; // required for constraint annotations

  Class<?>[] groups() default {}; // required for constraint annotations

  Class<? extends Payload>[] payload() default {}; // required for constraint annotations

  Class<? extends Validator<?>> use();

  @FunctionalInterface
  interface Validator<T> {
    boolean isValid(T value, ConstraintValidatorContext context);
  }
}
