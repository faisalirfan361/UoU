package com.UoU.infra.kafka;

public class NoRetryException extends RuntimeException {
  public NoRetryException(String message) {
    super(message);
  }

  public NoRetryException(Throwable cause) {
    super("Non-retryable exception", cause);
  }

  public NoRetryException(String message, Throwable cause) {
    super(message, cause);
  }
}
