package com.UoU.core.exceptions;

import java.util.Optional;
import java.util.function.Supplier;

public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public static NotFoundException ofName(String name) {
    return new NotFoundException(createDefaultMessage(name));
  }

  public static NotFoundException ofName(String name, Throwable cause) {
    return new NotFoundException(createDefaultMessage(name), cause);
  }

  public static NotFoundException ofClass(Class<?> cls) {
    return new NotFoundException(createDefaultMessage(cls.getSimpleName()));
  }

  public static NotFoundException ofClass(Class<?> cls, Throwable cause) {
    return new NotFoundException(createDefaultMessage(cls.getSimpleName()), cause);
  }

  private static String createDefaultMessage(String name) {
    return Optional.ofNullable(name)
        .filter(x -> !x.isBlank())
        .map(x -> x + " not found")
        .orElse("Not found");
  }

  public static <T> Optional<T> catchToOptional(Supplier<T> supplier) {
    try {
      return Optional.of(supplier.get());
    } catch (NotFoundException ex) {
      return Optional.empty();
    }
  }
}
