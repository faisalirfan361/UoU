package com.UoU.app;

import com.UoU.core.validation.Violation;
import java.util.List;

public record ErrorWithViolationsResponse(String error, List<Violation> violations) {
  private static final String DEFAULT_ERROR = "Invalid request";

  public ErrorWithViolationsResponse {
    error = error != null ? error : DEFAULT_ERROR;
  }

  public ErrorWithViolationsResponse(String error) {
    this(error, List.of());
  }

  public ErrorWithViolationsResponse(List<Violation> violations) {
    this(DEFAULT_ERROR, violations);
  }
}
