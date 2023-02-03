package com.UoU.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundStatusException extends ResponseStatusException {
  public NotFoundStatusException(@Nullable String reason, @Nullable Throwable cause) {
    super(HttpStatus.NOT_FOUND, reason, cause);
  }
}
