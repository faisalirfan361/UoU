package com.UoU.infra.db;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThatCode;

import com.UoU.core.exceptions.NotFoundException;
import java.util.function.Supplier;
import lombok.val;
import org.jooq.exception.NoDataFoundException;
import org.junit.jupiter.api.Test;

class ExceptionHelperTests {
  private static final ExceptionHelper HELPER = new ExceptionHelper(ExceptionHelperTests.class);

  @Test
  void throwNotFoundIfNoData_shouldReturnSuppliedValue() {
    Supplier<String> supplier = () -> "test";
    val result = HELPER.throwNotFoundIfNoData(supplier);
    assertThat(result).isEqualTo("test");
  }

  @Test
  void throwNotFoundIfNoData_shouldThrowNotFoundExceptionOnNoDataException() {
    Supplier<?> supplier = () -> {
      throw new NoDataFoundException("test");
    };

    assertThatCode(() -> HELPER.throwNotFoundIfNoData(supplier))
        .isInstanceOf(NotFoundException.class)
        .hasMessage(HELPER.notFound().getMessage());
  }

  @Test
  void throwNotFoundIfNoRowsAffected_shouldDoNothingForPositiveRows() {
    assertThatCode(() -> HELPER.throwNotFoundIfNoRowsAffected(1))
        .doesNotThrowAnyException();
  }

  @Test
  void throwNotFoundIfNoRowsAffected_shouldThrowNotFoundExceptionForZeroRows() {
    assertThatCode(() -> HELPER.throwNotFoundIfNoRowsAffected(0))
        .isInstanceOf(NotFoundException.class)
        .hasMessage(HELPER.notFound().getMessage());
  }
}
