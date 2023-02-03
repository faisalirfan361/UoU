package com.UoU.app.v1.mapping;

import com.UoU.app.v1.dtos.PageParamsDto;
import com.UoU.app.v1.dtos.PagedItems;
import com.UoU.core.PageParams;
import java.util.function.Function;

/**
 * Base mapper for a few things that are common across controllers (keep this small).
 *
 * <p>Normally, map methods should be reused with @Mapper(uses), but these are methods that the
 * child interfaces need to explicitly call, and so the child interfaces need to extend this one.
 */
public interface BaseMapper {
  PageParams toPageParamsModel(PageParamsDto dto);

  default <DtoT, ModelT> PagedItems<DtoT> toPagedItemsDto(
      com.UoU.core.PagedItems<ModelT> model, Function<ModelT, DtoT> itemMapper) {
    return new PagedItems<>(
        model.items().stream().map(itemMapper).toList(),
        new PagedItems.Meta(model.nextCursor()));
  }
}
