package com.UoU.core.events;

import com.UoU.core.AccessInfo;
import com.UoU.core.OrgId;
import lombok.NonNull;

public record EventAccessInfo(
    @NonNull OrgId orgId,
    boolean isReadOnly)
    implements AccessInfo<EventAccessInfo> {

  private static final String NAME = "Event";

  @Override
  public String name() {
    return NAME;
  }
}
