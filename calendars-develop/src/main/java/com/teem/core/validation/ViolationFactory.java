package com.UoU.core.validation;

import java.util.List;
import java.util.Optional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.val;
import org.springframework.validation.FieldError;

/**
 * Factory for creating simplified Violation records from javax violations and exceptions.
 */
public class ViolationFactory {
  public Violation create(ConstraintViolation<?> violation) {
    return new Violation(getFieldName(violation), violation.getMessage());
  }

  public Violation create(FieldError fieldError) {
    return new Violation(fieldError.getField(), fieldError.getDefaultMessage());
  }

  public List<Violation> createList(ConstraintViolationException ex) {
    return ex.getConstraintViolations().stream().map(this::create).toList();
  }

  public List<Violation> createList(List<FieldError> fieldErrors) {
    return fieldErrors.stream().map(this::create).toList();
  }

  private String getFieldName(ConstraintViolation<?> violation) {
    // If the object where the violation occurred implements ViolationFieldNameProvider, get the
    // custom field name from the object itself.
    if (violation.getLeafBean() instanceof ViolationFieldNameProvider) {
      val provider = ((ViolationFieldNameProvider) violation.getLeafBean());
      val result = provider
          .getViolationFieldName(violation)
          .filter(x -> !x.isEmpty());
      if (result.isPresent()) {
        return result.orElseThrow();
      }
    }

    // By default, return the full property path as name:
    return Optional
        .ofNullable(violation.getPropertyPath().toString())
        .filter(x -> !x.isEmpty())
        .orElse("<unknown>");
  }
}
