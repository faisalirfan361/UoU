package com.UoU.core.nylas.mapping;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;

import com.nylas.Calendar;
import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarCreateRequest;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarUpdateRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = NylasConfig.class)
public interface NylasCalendarMapper {
  @Mapping(target = "id", source = "calendarId")
  @Mapping(target = "externalId", source = "calendar.id")
  @Mapping(target = "isReadOnly", source = "calendar.readOnly")
  CalendarCreateRequest toCreateRequestModel(Calendar calendar, CalendarId calendarId, OrgId orgId);

  @Mapping(target = "id", source = "localCalendar.id")
  @Mapping(target = "orgId", source = "localCalendar.orgId")
  @Mapping(target = "name", source = "calendar.name")
  @Mapping(target = "isReadOnly", source = "calendar.readOnly")
  @Mapping(target = "timezone", source = "calendar.timezone", nullValueCheckStrategy = ALWAYS)
  CalendarUpdateRequest toUpdateRequestModel(
      Calendar calendar, com.UoU.core.calendars.Calendar localCalendar);

  @AfterMapping
  static void afterToUpdateRequestModel(
      @MappingTarget CalendarUpdateRequest.Builder builder,
      com.UoU.core.calendars.Calendar localCalendar) {

    // Unset fields that haven't changed from localCalendar for minimum update:
    builder.removeMatchingUpdateFields(localCalendar);
  }
}
