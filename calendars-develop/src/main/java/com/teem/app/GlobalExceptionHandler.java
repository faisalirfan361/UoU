package com.UoU.app;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.UoU.app.exceptions.NotFoundStatusException;
import com.UoU.core.admin.AdminOperationException;
import com.UoU.core.exceptions.IllegalOperationException;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.validation.ViolationException;
import com.UoU.core.validation.ViolationFactory;
import com.UoU.infra.db.InvalidCursorException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.NoDataFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Adds custom exception handling for specific things (spring defaults are fine for most stuff).
 */
@ControllerAdvice
@Slf4j
class GlobalExceptionHandler {

  // Private dependency, not injected:
  private final ViolationFactory violationFactory = new ViolationFactory();

  /**
   * Handles specific exceptions as 404s without having to rethrow as a NotFoundStatusException or
   * ResponseStatusException.
   */
  @ExceptionHandler({
      NotFoundException.class,
      NoDataFoundException.class,
      NotFoundStatusException.class
  })
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ResponseBody
  public ErrorResponse handleNotFound(Exception ex) {
    // Return helpful message for exception types where it's safe.
    if (ex instanceof NotFoundException || ex instanceof NotFoundStatusException) {
      return new ErrorResponse(ex.getMessage());
    }

    return new ErrorResponse("Not Found");
  }

  /**
   * Handles bad requests where the exception message is not safe to expose to callers.
   */
  @ExceptionHandler({
      HttpMessageNotReadableException.class,
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorWithViolationsResponse handleBadRequest(Exception ex) {
    // These types of errors can be tricky to debug, so log a debug msg:
    log.debug("Bad Request: {}", ex.getMessage());

    // Json errors are usually not safe to expose to callers, but make the message a little better.
    if (ex.getCause() instanceof JsonMappingException) {
      return new ErrorWithViolationsResponse(Optional
          .of(ex.getCause())
          .filter(x -> x instanceof InvalidTypeIdException)
          .map(x -> (InvalidTypeIdException) x)
          .map(x -> "Invalid request JSON: type '" + x.getTypeId() + "' is invalid")
          .orElse("Invalid request JSON"));
    }

    // Many exception messages will not be safe to expose because sql errors, etc. will come
    // through. So by default, just return that the request was bad.
    return new ErrorWithViolationsResponse("Bad Request");
  }

  /**
   * Handles bad requests where the exception message is safe to expose to callers.
   */
  @ExceptionHandler({
      ValidationException.class,
      IllegalOperationException.class,
      InvalidCursorException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorWithViolationsResponse handleSafeBadRequest(Exception ex) {
    return new ErrorWithViolationsResponse(ex.getMessage());
  }

  /**
   * Handles bad requests where we have a safe message and list of violations for callers.
   */
  @ExceptionHandler(ViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorWithViolationsResponse handleSafeBadRequestWithViolations(ViolationException ex) {
    return new ErrorWithViolationsResponse(ex.getMessage(), ex.getViolations());
  }

  /**
   * Handles javax validation constraint exceptions to provide a nice summary and 400 response.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorWithViolationsResponse handleConstraintValidationException(
      ConstraintViolationException ex) {
    return new ErrorWithViolationsResponse(violationFactory.createList(ex));
  }

  /**
   * Handles spring validation exceptions to provide a nice summary and 400 response.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorWithViolationsResponse handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    return new ErrorWithViolationsResponse(violationFactory.createList(ex.getFieldErrors()));
  }

  /**
   * Handles authorization exceptions for when a user doesn't have a required scope/permission.
   */
  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ResponseBody
  public ErrorResponse handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
    log.debug("Authorization failed with '{}': {}", ex.getMessage(), request.getRequestURI());
    return new ErrorResponse(ex.getMessage());
  }

  /**
   * Handles admin operation failures that have a message that's safe for admins to see.
   */
  @ExceptionHandler(AdminOperationException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorWithViolationsResponse handleAdminOperationException(AdminOperationException ex) {
    log.error("Admin operation failure", ex);
    return new ErrorWithViolationsResponse(ex.getMessage());
  }
}
