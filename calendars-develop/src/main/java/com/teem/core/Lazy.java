package com.UoU.core;

import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * Lazy value supplier that calls the wrapped supplier only once when needed and stores the result.
 *
 * <p>THIS IS NOT THREAD-SAFE, so be careful out there.
 */
@RequiredArgsConstructor
public class Lazy<T> {
  private final Supplier<T> supplier;
  private T value;
  private boolean isValueSet;

  public T get() {
    // Supplied value could be null, so check/set isValueSet instead of checking for null.
    if (!isValueSet) {
      value = supplier.get();
      isValueSet = true;
    }

    return value;
  }

  /**
   * Returns a supplier that will call get() on this instance and map to a new value when needed.
   *
   * <p>Note that only the underlying Lazy value is stored. The mapper func will run every time.
   * This is just a shortcut for calling get() and passing the results to the mapper func.
   */
  public <R> Supplier<R> map(Function<T, R> mapper) {
    return () -> mapper.apply(get());
  }
}
