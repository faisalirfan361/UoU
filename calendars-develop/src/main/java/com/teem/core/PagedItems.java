package com.UoU.core;

import java.util.List;

/**
 * Generic paged items wrapper.
 */
public record PagedItems<T>(
    List<T> items,
    String nextCursor) {

  public PagedItems {
    items = items != null ? items : List.of();
  }

  public boolean hasNextPage() {
    return nextCursor != null;
  }
}
