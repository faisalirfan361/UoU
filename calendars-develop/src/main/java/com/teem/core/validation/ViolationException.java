package com.UoU.core.validation;

import java.util.List;
import javax.validation.ValidationException;
import lombok.Getter;

/**
 * Extension of ValidationException that contains field violations for extra info.
 *
 * <p>This has the same meaning and should be handled the same way as ValidationException, but it
 * allows specifying field violations. The violations work much like javax ConstraintViolations,
 * but these are much simpler and also much easier to create (ConstraintViolations are a pain).
 */
public class ViolationException extends ValidationException {
  @Getter private final List<Violation> violations;

  public ViolationException(String message, Violation... violations) {
    super(message);
    this.violations = List.of(violations);
  }

  /**
   * Creates a ViolationException for a single field.
   */
  public static ViolationException forField(String field, String fieldMessage) {
    return new ViolationException("Invalid " + field, new Violation(field, fieldMessage));
  }

  /**
   * Creates a ViolationException with multiple fields that share the same error message.
   */
  public static ViolationException forFields(List<String> fields, String fieldMessage) {
    return new ViolationException(
        "Invalid fields",
        fields.stream().map(x -> new Violation(x, fieldMessage)).toArray(Violation[]::new));
  }
}
