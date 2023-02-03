package com.UoU.core.validation;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

/**
 * Wraps a {@link javax.validation.Validator} and provides some helpers for common things.
 *
 * <p>This helps us use the Validator to validate and build exceptions in a consistent way.
 * But you can still call {@link #getValidator()} and do more advanced things if needed.
 */
@AllArgsConstructor
@Service
public class ValidatorWrapper {
  private static final String DEFAULT_MESSAGE_FORMAT = "Invalid %s";

  @Getter private final Validator validator;

  /**
   * Validates and throws a {@link ConstraintViolationException} with a default message on failure.
   */
  public <T> T validateAndThrow(T object) {
    return validateAndThrow(
        object,
        () -> String.format(DEFAULT_MESSAGE_FORMAT, object.getClass().getSimpleName()));
  }

  /**
   * Validates and throws a {@link ConstraintViolationException} with the message on failure.
   */
  public <T> T validateAndThrow(T object, String message) {
    return validateAndHandle(object, violations -> {
      throw new ConstraintViolationException(message, violations);
    });
  }

  /**
   * Validates and throws a {@link ConstraintViolationException} with the message on failure.
   *
   * <p>Use this method when the message needs constructing so extra work can be avoided on success.
   */
  public <T> T validateAndThrow(T object, Supplier<String> message) {
    return validateAndHandle(object, violations -> {
      throw new ConstraintViolationException(message.get(), violations);
    });
  }

  /**
   * Validates the object and returns it, passing violations to the handler on failure.
   *
   * <p>The result won't be null because a null object will cause a {@link ValidationException}.
   */
  public @NotNull <T> T validateAndHandle(T object, Consumer<Set<ConstraintViolation<T>>> handler) {
    Set<ConstraintViolation<T>> violations;

    try {
      violations = validator.validate(object);
    } catch (IllegalArgumentException ex) {
      // If object was null or otherwise rejected before getting to constraint-checking, treat that
      // as a validation failure as well.
      throw new ValidationException(ex);
    }

    if (!violations.isEmpty()) {
      handler.accept(violations);
    }

    return object;
  }
}
