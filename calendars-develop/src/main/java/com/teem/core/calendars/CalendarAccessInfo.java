package com.UoU.core.calendars;

import com.UoU.core.AccessInfo;
import com.UoU.core.OrgId;
import lombok.NonNull;

public record CalendarAccessInfo(
    @NonNull OrgId orgId,
    boolean isReadOnly)
    implements AccessInfo<CalendarAccessInfo> {

  private static final String NAME = "Calendar";

  @Override public String name() {
    return NAME;
  }
}
