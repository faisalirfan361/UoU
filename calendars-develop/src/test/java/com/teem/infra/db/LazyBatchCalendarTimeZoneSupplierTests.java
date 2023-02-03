package com.UoU.infra.db;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.val;
import org.junit.jupiter.api.Test;

class LazyBatchCalendarTimeZoneSupplierTests {

  @Test
  @SuppressWarnings("unchecked")
  void mapSupplier_shouldOnlyBeCalledOnceWhenFirstNeeded() {
    val keys = IntStream.range(1, 5).boxed().toList();
    val zoneMap = keys.stream().collect(Collectors.toMap(x -> x, x -> "UTC"));

    Supplier<Map<Integer, String>> mapSupplierMock = mock(Supplier.class);
    when(mapSupplierMock.get()).thenReturn(zoneMap);

    val batchSupplier = new LazyBatchCalendarTimeZoneSupplier<>(mapSupplierMock);
    val innerSuppliers = keys
        .stream()
        .map(batchSupplier::createSupplier)
        .toList();

    verifyNoInteractions(mapSupplierMock);
    innerSuppliers.forEach(Supplier::get); // called exactly once here
    verify(mapSupplierMock).get();
  }

  @Test
  void createSupplier_shouldReturnSupplierThatThrowsForMissingTimezone() {
    val batchSupplier = new LazyBatchCalendarTimeZoneSupplier<>(() -> Map.of());
    val createdSupplier = batchSupplier.createSupplier("x");

    assertThatCode(() -> createdSupplier.get())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("missing timezone");
  }

  @Test
  void createSupplier_shouldReturnSupplierThatThrowsForInvalidTimeZone() {
    val id = 1;
    val zoneMap = Map.of(id, "invalid");
    val batchSupplier = new LazyBatchCalendarTimeZoneSupplier<>(() -> zoneMap);
    val createdSupplier = batchSupplier.createSupplier(id);

    assertThatCode(() -> createdSupplier.get())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("invalid timezone");
  }
}
