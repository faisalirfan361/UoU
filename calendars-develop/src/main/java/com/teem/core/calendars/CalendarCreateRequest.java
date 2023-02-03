package com.UoU.core.calendars;

import com.UoU.core.OrgId;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.validation.annotations.TimeZone;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record CalendarCreateRequest(
    @NotNull @Valid CalendarId id,
    @Valid CalendarExternalId externalId,
    @Valid AccountId accountId,
    @NotNull @Valid OrgId orgId,
    @Size(max = CalendarConstraints.NAME_MAX) String name,
    Boolean isReadOnly,
    @TimeZone String timezone
) {

  @lombok.Builder(builderClassName = "Builder")
  public CalendarCreateRequest {
  }
}
