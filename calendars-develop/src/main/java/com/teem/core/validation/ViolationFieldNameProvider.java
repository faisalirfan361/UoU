package com.UoU.core.validation;

import java.util.Optional;
import javax.validation.ConstraintViolation;

public interface ViolationFieldNameProvider {
  Optional<String> getViolationFieldName(ConstraintViolation<?> violation);
}
