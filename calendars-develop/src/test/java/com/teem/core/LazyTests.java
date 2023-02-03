package com.UoU.core;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class LazyTests {

  @Test
  void get_shouldCallSupplierOnlyOnce() {
    var value = 123;
    var supplierMock = mock(Supplier.class);
    when(supplierMock.get()).thenReturn(value);

    var lazy = new Lazy<Integer>(supplierMock);
    verifyNoInteractions(supplierMock);

    lazy.get();
    lazy.get();
    var result = lazy.get();

    assertThat(result).isEqualTo(value);
    verify(supplierMock, times(1)).get();
  }

  @Test
  void map_shouldCallSupplierOnlyOnce() {
    var supplierMock = mock(Supplier.class);
    when(supplierMock.get()).thenReturn("lazy");

    var lazy = new Lazy<String>(supplierMock);
    var mapped = lazy.map(x -> x + "-mapped");
    verifyNoInteractions(supplierMock);

    var lazyResult = lazy.get();
    var mappedResult = mapped.get();
    lazy.get();

    assertThat(lazyResult).isEqualTo("lazy");
    assertThat(mappedResult).isEqualTo("lazy-mapped");
    verify(supplierMock, times(1)).get();
  }
}
