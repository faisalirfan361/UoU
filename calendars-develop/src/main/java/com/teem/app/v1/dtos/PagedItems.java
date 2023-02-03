package com.UoU.app.v1.dtos;

import com.UoU.app.docs.SchemaExt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Generic paged items wrapper.
 *
 * <p>This is named without the "Dto" suffix because we can't use the @Schema annotation like normal
 * on generic classes or Sprindoc won't handle the generic variations correctly.
 */
@Schema(requiredProperties = SchemaExt.Required.ALL)
public record PagedItems<T>(
    List<T> items,
    Meta meta
) {

  public PagedItems {
    items = items != null ? items : List.of();
    meta = meta != null ? meta : new Meta(null);
  }

  @Schema(name = "PagedItemsMeta")
  public record Meta(
      @Schema(
          description = "Cursor to get the next page, if any, via the **cursor** query param",
          nullable = true)
      String nextCursor
  ) {
  }
}
