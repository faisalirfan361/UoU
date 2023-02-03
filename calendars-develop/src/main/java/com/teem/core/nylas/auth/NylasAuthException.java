package com.UoU.core.nylas.auth;

/**
 * Exception for a Nylas auth_error or bad auth request, such as for bad permissions or 403s.
 *
 * <p>This indicates a problem with a known solution where we can tell the user to fix something.
 * It does not include network issues or any number of other things that could fail during an auth
 * request. The message should be user-safe so we can let the user know what went wrong. We should
 * usually include the failure message from Nylas, plus some context so it makes more sense.
 */
public class NylasAuthException extends RuntimeException {
  public NylasAuthException(String message, Throwable cause) {
    super(message, cause);
  }
}
