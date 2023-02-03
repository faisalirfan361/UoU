package com.UoU.core.exceptions;

public class ReadOnlyException extends IllegalOperationException {
  public ReadOnlyException(String message) {
    super(message);
  }
}
