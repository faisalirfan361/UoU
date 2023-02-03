package com.UoU.core.admin;

/**
 * Exception for a failed admin operation where we can tell the admin what happened.
 */
public class AdminOperationException extends RuntimeException {
  public AdminOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
