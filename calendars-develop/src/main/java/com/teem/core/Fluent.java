package com.UoU.core;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NonNull;

/**
 * Helper class for working with objects fluently to build, map, initialize, etc.
 *
 * <p>You can use {@link #map(Function)} to transform the object into something else, and you can
 * use {@link #also(Consumer)} to perform side effects and continue on with the existing object.
 *
 * <p>This works much like Optional and Stream, but it provides slightly different semantics so
 * those classes aren't (mis)used in ways that are confusing.
 *
 * <p>Example:
 * <pre>{@code
 * var stuff = Fluent.of(new Thing()).map(x -> Stuff(x.name)).also(x -> log.info(x.name)).get();
 * }</pre>
 */
public class Fluent<T> {
  private final T value;

  public static <T> Fluent<T> of(@NonNull T value) {
    return new Fluent<>(value);
  }

  private Fluent(T value) {
    this.value = value;
  }

  /**
   * Maps to a new type via a mapper func.
   */
  public <R> Fluent<R> map(Function<T, R> mapper) {
    return new Fluent<>(mapper.apply(value));
  }

  /**
   * Performs a side effect via a consumer and returns the original object.
   */
  public Fluent<T> also(Consumer<T> consumer) {
    consumer.accept(value);
    return this;
  }

  /**
   * Calls {@link #also(Consumer)} only if the condition is true.
   */
  public Fluent<T> ifThenAlso(boolean condition, Consumer<T> consumer) {
    if (condition) {
      return also(consumer);
    }
    return this;
  }

  /**
   * Calls {@link #also(Consumer)} only if the condition returns true.
   */
  public Fluent<T> ifThenAlso(Function<T, Boolean> condition, Consumer<T> consumer) {
    if (condition.apply(value)) {
      return also(consumer);
    }
    return this;
  }

  /**
   * Calls the consumer with the Fluent and Optional values if the Optional has a value.
   */
  public <OptionalT> Fluent<T> ifThenAlso(
      Optional<OptionalT> condition, BiConsumer<T, OptionalT> consumer) {
    condition.ifPresent(x -> consumer.accept(value, x));
    return this;
  }

  /**
   * Checks the condition and throws the supplied exception if true.
   */
  public <X extends Throwable> Fluent<T> ifThenThrow(
      Function<T, Boolean> condition, Supplier<X> exceptionSupplier) throws X {
    if (condition.apply(value)) {
      throw exceptionSupplier.get();
    }
    return this;
  }

  /**
   * Returns the underlying object instance.
   */
  public T get() {
    return value;
  }
}
