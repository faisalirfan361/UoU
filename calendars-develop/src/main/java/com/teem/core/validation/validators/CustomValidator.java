package com.UoU.core.validation.validators;

import com.UoU.core.validation.annotations.Custom;
import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomValidator implements ConstraintValidator<Custom, Object> {
  private Custom.Validator validator;

  @Override
  @SneakyThrows
  public void initialize(Custom annotation) {
    var ctor = Optional
        .ofNullable(annotation.use())
        .map(x -> x.getDeclaredConstructors())
        .filter(x -> x.length == 1)
        .map(x -> x[0])
        .orElseThrow(() -> new IllegalArgumentException(
            "Validator must have a single parameterless constructor"));

    validator = Optional
        .ofNullable((Custom.Validator) ctor.newInstance())
        .orElseThrow(() -> new IllegalArgumentException("Validator could not be created"));
  }

  @Override
  @SuppressWarnings("unchecked") // because we have to use generic Object for annotation
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    try {
      return validator.isValid(value, context);
    } catch (Exception ex) {
      log.error("Error in custom validator", ex);
      throw ex;
    }
  }
}
