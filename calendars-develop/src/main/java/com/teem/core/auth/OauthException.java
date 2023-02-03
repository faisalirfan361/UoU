package com.UoU.core.auth;

/**
 * Indicates an unexpected OAuth failure that is not some more specific, known case.
 */
public class OauthException extends RuntimeException {
  public OauthException(String message) {
    super(message);
  }

  public OauthException(String message, Throwable cause) {
    super(message, cause);
  }
}
