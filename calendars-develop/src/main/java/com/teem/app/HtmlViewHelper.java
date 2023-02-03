package com.UoU.app;

import com.UoU.core.Fluent;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

/**
 * Helper for creating common HTML views, such as for redirects and error/success pages.
 */
@Service
@Slf4j
public class HtmlViewHelper {

  /**
   * Creates a view that redirects to the passed url or path.
   */
  public ModelAndView redirect(String url) {
    return new ModelAndView("redirect:" + url);
  }

  /**
   * Creates a view that forwards (server side) to the passed path.
   */
  public ModelAndView forward(String path) {
    return new ModelAndView("forward:" + path);
  }

  /**
   * Creates a success view that displays the passed message.
   */
  public ModelAndView success(String message) {
    return Fluent
        .of(new ModelAndView("message", Map.of("success", message)))
        .also(x -> x.setStatus(HttpStatus.OK))
        .get();
  }

  /**
   * Creates a basic error view.
   */
  public ModelAndView error(HttpStatus status, String message) {
    return error(status, message, null);
  }

  /**
   * Creates an error view that also includes more details about the error.
   */
  public ModelAndView error(HttpStatus status, String message, String details) {
    return Fluent
        .of(new ModelAndView("message", Map.of("error", message)))
        .ifThenAlso(x -> details != null, x -> x.getModelMap().addAttribute("details", details))
        .also(x -> x.setStatus(status))
        .get();
  }

  /**
   * Creates an error view with a unique error id that's also logged to match what the user sees.
   */
  public ModelAndView errorWithLoggedId(HttpStatus status, String message, Exception cause) {
    val errorId = UUID.randomUUID();

    // For client errors (like 400), log as debug, not error, since it's not an error for us.
    val logMessage = "{} (Cause: {}, Error Id: {}, Status: {})";
    if (status.is4xxClientError()) {
      log.debug(logMessage, message, cause.getMessage(), errorId, status.value(), cause);
    } else {
      log.error(logMessage, message, cause.getMessage(), errorId, status.value(), cause);
    }

    return error(status, message, "Error Id: " + errorId);
  }

  /**
   * Catches any exception caused by the supplier and returns a 500 error view.
   */
  public ModelAndView catchAndReturnInternalServerError(Supplier<ModelAndView> supplier) {
    try {
      return supplier.get();
    } catch (Exception ex) {
      return errorWithLoggedId(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Sorry, something went wrong on our end.",
          ex);
    }
  }
}
