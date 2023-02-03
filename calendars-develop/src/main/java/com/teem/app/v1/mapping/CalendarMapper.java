package com.UoU.app.v1.mapping;

import com.UoU.app.v1.dtos.CalendarDto;
import com.UoU.app.v1.dtos.CalendarUpdateRequestDto;
import com.UoU.app.v1.dtos.InternalCalendarBatchCreateRequestDto;
import com.UoU.app.v1.dtos.InternalCalendarBatchInfoDto;
import com.UoU.app.v1.dtos.InternalCalendarCreateRequestDto;
import com.UoU.app.v1.dtos.InternalCalendarInfoDto;
import com.UoU.app.v1.dtos.PagedItems;
import com.UoU.core.OrgId;
import com.UoU.core.calendars.Calendar;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.calendars.CalendarUpdateRequest;
import com.UoU.core.calendars.InternalCalendarBatchCreateRequest;
import com.UoU.core.calendars.InternalCalendarCreateRequest;
import com.UoU.core.calendars.InternalCalendarInfo;
import com.UoU.core.mapping.Config;
import com.UoU.core.mapping.WrappedValueMapper;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = Config.class, uses = WrappedValueMapper.class)
public interface CalendarMapper extends BaseMapper {
  CalendarDto toCalendarDto(Calendar model);

  default PagedItems<CalendarDto> toPagedCalendarsDto(com.UoU.core.PagedItems<Calendar> model) {
    return toPagedItemsDto(model, this::toCalendarDto);
  }

  @Mapping(target = "isReadOnly", ignore = true)
  @Mapping(target = "removeMatchingUpdateFields", ignore = true)
  CalendarUpdateRequest toUpdateRequestModel(
      CalendarUpdateRequestDto request, CalendarId id, OrgId orgId);

  InternalCalendarCreateRequest toInternalCalendarCreateRequestModel(
      InternalCalendarCreateRequestDto request, OrgId orgId);

  @Mapping(target = "isDryRun", source = "request.dryRun")
  InternalCalendarBatchCreateRequest toInternalCalendarBatchCreateRequestModel(
      InternalCalendarBatchCreateRequestDto request, OrgId orgId);

  InternalCalendarInfoDto toInternalCalendarInfoDto(InternalCalendarInfo info);

  InternalCalendarBatchInfoDto toInternalCalendarBatchInfoDto(
      Map<Integer, InternalCalendarInfo> infos);
}
