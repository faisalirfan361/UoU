package com.UoU.app.v1.mapping;

import com.UoU.app.security.Principal;
import com.UoU.app.v1.dtos.ConferencingMeetingRequestDto;
import com.UoU.app.v1.dtos.EventCreateRequestDto;
import com.UoU.app.v1.dtos.EventDto;
import com.UoU.app.v1.dtos.EventUpdateRequestDto;
import com.UoU.app.v1.dtos.PagedItems;
import com.UoU.app.v1.dtos.ParticipantDto;
import com.UoU.app.v1.dtos.RecurrenceDto;
import com.UoU.app.v1.dtos.WhenDto;
import com.UoU.app.v1.dtos.WhenParamsDto;
import com.UoU.core.OrgId;
import com.UoU.core.conferencing.ConferencingMeetingCreateRequest;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.Event;
import com.UoU.core.events.EventCreateRequest;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventQuery;
import com.UoU.core.events.EventRequest;
import com.UoU.core.events.EventUpdateRequest;
import com.UoU.core.events.ParticipantRequest;
import com.UoU.core.events.Recurrence;
import com.UoU.core.events.When;
import com.UoU.core.mapping.Config;
import com.UoU.core.mapping.WrappedValueMapper;
import java.util.Optional;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = Config.class, uses = WrappedValueMapper.class)
public interface EventMapper extends BaseMapper {

  @Mapping(target = "orgId", expression = Expressions.PRINCIPAL_ORG_ID)
  @Mapping(target = "externalId", ignore = true)
  @Mapping(target = "icalUid", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "isReadOnly", ignore = true)
  @Mapping(target = "checkinAt", ignore = true)
  @Mapping(target = "checkoutAt", ignore = true)
  @Mapping(target = "owner", ignore = true)
  @Mapping(target = "recurrence", source = "request")
  @Mapping(target = "conferencing", source = "request.conferencing.autoCreate")
  EventCreateRequest toRequestModel(
      EventCreateRequestDto request, EventId id, @Context Principal principal);

  @Mapping(target = "principalEmail", expression = Expressions.PRINCIPAL_SUBJECT)
  ConferencingMeetingCreateRequest toRequestModel(
      ConferencingMeetingRequestDto.ConferencingMeetingAutoCreate autoCreate,
      @Context Principal principal);

  @Mapping(target = "externalId", ignore = true)
  @Mapping(target = "icalUid", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "isReadOnly", ignore = true)
  @Mapping(target = "owner", ignore = true)
  @Mapping(target = "removeUpdateFields", ignore = true)
  @Mapping(target = "removeMatchingUpdateFields", ignore = true)
  @Mapping(target = "recurrence", source = "request")
  EventUpdateRequest toRequestModel(EventUpdateRequestDto request, EventId id, OrgId orgId);

  EventRequest toRequestModel(EventId id, OrgId orgId, String dataSource);

  default Recurrence toRecurrenceModel(EventCreateRequestDto request) {
    return Optional
        .ofNullable(request)
        .map(x -> x.recurrence())
        .map(x -> Recurrence.master(toRecurrenceMasterModel(x, request.when())))
        .orElse(Recurrence.none());
  }

  default Recurrence.Master toRecurrenceMasterModel(EventUpdateRequestDto request) {
    return request != null
        ? toRecurrenceMasterModel(request.recurrence(), request.when())
        : null;
  }

  default Recurrence.Master toRecurrenceMasterModel(RecurrenceDto dto, WhenDto when) {
    return dto == null ? null : Optional
        .ofNullable(when)
        .map(x -> switch (x.type()) {
          case DATE, DATESPAN -> true; // is all-day
          default -> false; // not all-day
        })
        .map(Recurrence.Master.ValidationContext::new)
        .map(context -> new Recurrence.Master(dto.rrule(), dto.timezone(), context))
        .orElseGet(() -> new Recurrence.Master(dto.rrule(), dto.timezone()));
  }

  RecurrenceDto toRecurrenceDto(Recurrence.Master recurrence);

  /**
   * Maps to participant request for create/update, which for API requests is only name and email.
   */
  @Mapping(target = "comment", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "removeMatchingUpdateFields", ignore = true)
  ParticipantRequest toParticipantRequestModel(ParticipantDto dto);

  /**
   * Maps a string to an API data source because all input in this context comes from the API.
   */
  default DataSource toApiDataSourceModel(String apiDataSource) {
    return DataSource.fromApi(apiDataSource);
  }

  @Mapping(target = "recurrence", source = "model.recurrence.master")
  @Mapping(target = "recurrenceInstance", source = "model.recurrence.instance")
  @Mapping(target = "debugInfo", source = "model", conditionExpression = "java(includeDebugInfo)")
  EventDto toEventDto(Event model, boolean includeDebugInfo);

  default EventDto toEventDto(Event model) {
    return toEventDto(model, false);
  }

  default PagedItems<EventDto> toPagedEventsDto(
      com.UoU.core.PagedItems<Event> model, boolean includeDebugInfo) {
    return toPagedItemsDto(model, x -> toEventDto(x, includeDebugInfo));
  }

  default When toWhenModel(WhenDto dto) {
    if (dto == null) {
      return null;
    }

    When result;
    switch (dto.type()) {
      case TIMESPAN -> result = toTimeSpanModel((WhenDto.TimeSpan) dto);
      case DATESPAN -> result = toDateSpanModel((WhenDto.DateSpan) dto);
      case DATE -> result = toDateModel((WhenDto.Date) dto);
      default -> throw new IllegalArgumentException(
          "Unknown 'when' type in event: " + dto.type().name());
    }

    return result;
  }

  default WhenDto toWhenDto(When model) {
    if (model == null) {
      return null;
    }

    WhenDto result;
    switch (model.type()) {
      case TIMESPAN -> result = toTimeSpanDto((When.TimeSpan) model);
      case DATESPAN -> result = toDateSpanDto((When.DateSpan) model);
      case DATE -> result = toDateDto((When.Date) model);
      default -> throw new IllegalArgumentException(
          "Unknown 'when' type in event: " + model.type().name());
    }

    return result;
  }

  When.TimeSpan toTimeSpanModel(WhenDto.TimeSpan dto);

  WhenDto.TimeSpan toTimeSpanDto(When.TimeSpan model);

  @Mapping(target = "effectiveUtcTimeSpan", ignore = true)
  When.DateSpan toDateSpanModel(WhenDto.DateSpan dto);

  default WhenDto.DateSpan toDateSpanDto(When.DateSpan model) {
    return model == null ? null : model
        .effectiveUtcTimeSpan()
        .map(x -> new WhenDto.DateSpan(model.startDate(), model.endDate(), x.start(), x.end()))
        .orElseGet(() -> new WhenDto.DateSpan(model.startDate(), model.endDate()));
  }

  @Mapping(target = "effectiveUtcTimeSpan", ignore = true)
  When.Date toDateModel(WhenDto.Date dto);

  default WhenDto.Date toDateDto(When.Date model) {
    return model == null ? null : model
        .effectiveUtcTimeSpan()
        .map(x -> new WhenDto.Date(model.date(), x.start(), x.end()))
        .orElseGet(() -> new WhenDto.Date(model.date()));
  }

  EventQuery.WhenQuery toWhenQueryModel(WhenParamsDto dto);
}
