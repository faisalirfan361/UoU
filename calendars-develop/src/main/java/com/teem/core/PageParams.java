package com.UoU.core;

public record PageParams(
    String cursor,
    Integer limit
) {
  public static final PageParams DEFAULT = new PageParams(null, DataConfig.Paging.DEFAULT_LIMIT);

  public PageParams {
    limit = limit == null || limit == 0
        ? DataConfig.Paging.DEFAULT_LIMIT
        : Math.min(DataConfig.Paging.MAX_LIMIT, Math.max(1, limit));
  }
}
