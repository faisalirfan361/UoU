package com.UoU.app.v1.mapping;

import com.UoU.app.v1.dtos.AvailabilityRequestDto;
import com.UoU.app.v1.dtos.AvailabilityResponseDto;
import com.UoU.app.v1.dtos.EventTimeSpanDto;
import com.UoU.app.v1.dtos.FreeBusyDetailedResponseDto;
import com.UoU.app.v1.dtos.FreeBusyResponseDto;
import com.UoU.app.v1.dtos.ItemsByIdDto;
import com.UoU.app.v1.dtos.TimeSpanDto;
import com.UoU.core.OrgId;
import com.UoU.core.TimeSpan;
import com.UoU.core.calendars.AvailabilityRequest;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.EventTimeSpan;
import com.UoU.core.mapping.Config;
import com.UoU.core.mapping.WrappedValueMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = Config.class, uses = WrappedValueMapper.class)
public interface AvailabilityMapper {

  AvailabilityRequest toAvailabilityRequest(AvailabilityRequestDto request, OrgId orgId);

  AvailabilityResponseDto toAvailabilityResponseDto(Map<CalendarId, Boolean> itemsById);

  FreeBusyResponseDto toFreeBusyResponseDto(Map<CalendarId, List<TimeSpan>> itemsById);

  FreeBusyDetailedResponseDto toFreeBusyDetailedResponseDto(
      Map<CalendarId, List<EventTimeSpan>> itemsById);

  default ItemsByIdDto<List<TimeSpanDto>> toTimeSpanItemsByIdDto(
      Map<CalendarId, List<TimeSpan>> itemsById) {
    return itemsById == null ? null : new ItemsByIdDto<>(
        itemsById.entrySet().stream().collect(
            Collectors.toMap(
                x -> x.getKey().value(),
                x -> x.getValue().stream().map(this::toTimeSpanDto).toList())));
  }

  default ItemsByIdDto<List<EventTimeSpanDto>> toEventTimeSpanItemsByIdDto(
      Map<CalendarId, List<EventTimeSpan>> itemsById) {
    return itemsById == null ? null : new ItemsByIdDto<>(
        itemsById.entrySet().stream().collect(
            Collectors.toMap(
                x -> x.getKey().value(),
                x -> x.getValue().stream().map(this::toEventTimeSpanDto).toList())));
  }

  @Mapping(target = "maxDuration", ignore = true)
  @Mapping(target = "withMaxDuration", ignore = true)
  TimeSpan toTimeSpan(TimeSpanDto dto);

  TimeSpanDto toTimeSpanDto(TimeSpan model);

  EventTimeSpanDto toEventTimeSpanDto(EventTimeSpan model);
}
