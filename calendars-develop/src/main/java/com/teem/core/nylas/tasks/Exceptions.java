package com.UoU.core.nylas.tasks;

import com.nylas.RequestFailedException;

/**
 * Package-private helper for working with Nylas exceptions (not injected, implementation detail).
 */
class Exceptions {

  /**
   * Returns true if the Nylas API exception is an HTTP 404 Not Found.
   */
  public static boolean isNotFound(RequestFailedException ex) {
    return ex != null && ex.getStatusCode() == 404;
  }

  /**
   * Returns true if the Nylas API exception is an HTTP 422 Unprocessable Entity.
   */
  public static boolean isUnprocessableEntity(RequestFailedException ex) {
    return ex != null && ex.getStatusCode() == 422;
  }
}
