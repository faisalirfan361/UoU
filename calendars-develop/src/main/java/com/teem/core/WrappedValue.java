package com.UoU.core;

import com.UoU.core.validation.ViolationFieldNameProvider;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import javax.validation.ConstraintViolation;
import lombok.val;

/**
 * Interface for semantic wrapped value objects such as ids.
 *
 * <p>This serves two main purposes:
 * - Ensures a consistent naming convention for all wrapped values.
 * - Helps tools such as mappers and serializers extract the inner value in a common way.
 */
public interface WrappedValue<T> extends ViolationFieldNameProvider {
  T value();

  /**
   * Provides a custom violation field name that omits the "value" part of the path.
   *
   * <p>In most contexts, a violation field name of "value" (the actual field name) won't make
   * sense, so this returns the field based on the violation path as follows:
   * If the path is 1 level, the lowercased class name is used instead ("someWrappedValue").
   * If the path is 2 or more levels, the final ".value" portion is removed, so
   * "wrapper1.wrapper2.value" becomes "wrapper1.wrapper2".
   */
  @Override
  default Optional<String> getViolationFieldName(ConstraintViolation<?> violation) {
    return Optional.ofNullable(violation.getPropertyPath()).flatMap(path -> {
      val parts = new ArrayList<String>();
      path.forEach(node -> parts.add(node.getName()));

      if (parts.size() == 1) {
        // Instead of "value", use the lowercased class name instead, like "someWrappedValue":
        return Optional
            .of(getClass().getSimpleName())
            .map(x -> x.substring(0, 1).toLowerCase(Locale.ROOT) + x.substring(1));
      } else if (parts.size() >= 2) {
        // Remove the final ".value" path, like "wrapper.value" -> "wrapper":
        return Optional.of(String.join(".", parts.stream().limit(parts.size() - 1).toList()));
      }

      return Optional.empty(); // Return empty so caller can do default behavior
    });
  }
}
