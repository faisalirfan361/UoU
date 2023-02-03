package com.UoU.infra.db;

public class InvalidCursorException extends RuntimeException {
  private static final String MESSAGE = "Invalid paging cursor";

  public InvalidCursorException() {
    super(MESSAGE);
  }

  public InvalidCursorException(Throwable cause) {
    super(MESSAGE, cause);
  }
}
