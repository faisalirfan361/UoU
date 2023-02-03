package com.UoU.app.v1.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.Delegate;

@Schema(name = "ItemsById")
public class ItemsByIdDto<T> implements Map<String, T> {
  @Delegate
  private final Map<String, T> map;

  public ItemsByIdDto() {
    this(new HashMap<>());
  }

  public ItemsByIdDto(Map<String, T> map) {
    this.map = map;
  }
}
