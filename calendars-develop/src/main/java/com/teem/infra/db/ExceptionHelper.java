package com.UoU.infra.db;

import com.UoU.core.exceptions.NotFoundException;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.jooq.exception.NoDataFoundException;

/**
 * Package private helper for common jooq exception throwing and handling.
 *
 * <p>This is an internal dependency that is not injected, so nothing outside the db package
 * should know about it because it's an implementation detail.
 */
@AllArgsConstructor
class ExceptionHelper {
  private final Class<?> cls;

  /**
   * Creates a NotFoundException for the helper class.
   */
  public NotFoundException notFound() {
    return NotFoundException.ofClass(cls);
  }

  /**
   * Throws NotFoundException if the supplier causes a jooq NoDataFoundException.
   *
   * <p>This helps return a common exception so we don't use a mix of similar exceptions.
   */
  public <T> T throwNotFoundIfNoData(Supplier<T> supplier) {
    try {
      return supplier.get();
    } catch (NoDataFoundException ex) {
      throw NotFoundException.ofClass(cls, ex);
    }
  }

  /**
   * Throws NotFoundException if no rows were affected, as in for updates or deletes.
   */
  public void throwNotFoundIfNoRowsAffected(int rowsAffected) {
    if (rowsAffected == 0) {
      throw notFound();
    }
  }
}
