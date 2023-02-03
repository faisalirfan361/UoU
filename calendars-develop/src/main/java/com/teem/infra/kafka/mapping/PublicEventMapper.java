package com.UoU.infra.kafka.mapping;

import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.Event;
import com.UoU.core.events.EventId;
import com.UoU.core.events.Participant;
import com.UoU.core.events.ParticipantStatus;
import com.UoU.core.events.Recurrence;
import com.UoU.core.events.When;
import com.UoU.core.mapping.WrappedValueMapper;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.mapstruct.EnumMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ValueMapping;

@Mapper(
    componentModel = "spring", // generate spring @Component for DI
    injectionStrategy = InjectionStrategy.CONSTRUCTOR, // use ctor injection like our own code
    uses = WrappedValueMapper.class)
public interface PublicEventMapper {

  @Mapping(target = "eventId", source = "event.id")
  @Mapping(target = "event", source = "event")
  @Mapping(target = "eventBuilder", ignore = true)
  com.UoU.infra.avro.publicevents.EventChanged toEventChangedAvro(
      Event event,
      com.UoU.infra.avro.publicevents.EventChangeType changeType,
      DataSource changeSource);

  @Mapping(target = "changeType", expression = "java(EventChangeType.deleted)")
  @Mapping(target = "event", ignore = true)
  @Mapping(target = "eventBuilder", ignore = true)
  com.UoU.infra.avro.publicevents.EventChanged toEventChangedDeletedAvro(
      OrgId orgId, CalendarId calendarId, EventId eventId, DataSource changeSource);

  @Mapping(target = "whenBuilder", ignore = true)
  @Mapping(target = "recurrenceBuilder", ignore = true)
  @Mapping(target = "recurrenceInstanceBuilder", ignore = true)
  @Mapping(target = "recurrenceInstance", source = "event.recurrence")
  @Mapping(target = "ownerBuilder", ignore = true)
  com.UoU.infra.avro.publicevents.Event toEventAvro(Event event);

  default com.UoU.infra.avro.publicevents.When toWhenAvro(When when) {

    val builder = com.UoU.infra.avro.publicevents.When.newBuilder();
    switch (when.type()) {
      case TIMESPAN -> builder
          .setType(com.UoU.infra.avro.publicevents.WhenType.timespan)
          .setData(toWhenTimeSpanAvro((When.TimeSpan) when));
      case DATESPAN -> builder
          .setType(com.UoU.infra.avro.publicevents.WhenType.datespan)
          .setData(toWhenDateSpanAvro((When.DateSpan) when));
      case DATE -> builder
          .setType(com.UoU.infra.avro.publicevents.WhenType.date)
          .setData(toWhenDateAvro((When.Date) when));
      default -> throw new IllegalArgumentException("Unexpected when type: " + when.type());
    }
    return builder.build();
  }

  com.UoU.infra.avro.publicevents.WhenTimeSpan toWhenTimeSpanAvro(When.TimeSpan timeSpan);

  default com.UoU.infra.avro.publicevents.WhenDateSpan toWhenDateSpanAvro(When.DateSpan dateSpan) {

    return Optional
        .ofNullable(dateSpan)
        .map(x -> {
          val utcTimeSpan = dateSpan.effectiveUtcTimeSpan().orElseThrow(() ->
              new IllegalStateException("Missing event effectiveUtcTimeSpan"));
          return com.UoU.infra.avro.publicevents.WhenDateSpan.newBuilder()
              .setStartDate(x.startDate())
              .setEndDate(x.endDate())
              .setEffectiveUtcStartTime(utcTimeSpan.start())
              .setEffectiveUtcEndTime(utcTimeSpan.end())
              .build();
        })
        .orElse(null);
  }

  default com.UoU.infra.avro.publicevents.WhenDate toWhenDateAvro(When.Date date) {

    return Optional
        .ofNullable(date)
        .map(x -> {
          val utcTimeSpan = date.effectiveUtcTimeSpan().orElseThrow(() ->
              new IllegalStateException("Missing event effectiveUtcTimeSpan"));
          return com.UoU.infra.avro.publicevents.WhenDate.newBuilder()
              .setDate(x.date())
              .setEffectiveUtcStartTime(utcTimeSpan.start())
              .setEffectiveUtcEndTime(utcTimeSpan.end())
              .build();
        })
        .orElse(null);
  }

  default com.UoU.infra.avro.publicevents.Recurrence toRecurrenceAvro(Recurrence recurrence) {
    return Optional
        .ofNullable(recurrence)
        .flatMap(x -> x.withMaster())
        .map(this::toRecurrenceAvro)
        .orElse(null);
  }

  // TODO: Recurrence timezone is never supposed to be null, and the avro schema doesn't allow it.
  // But for some reason, Google is returning the value as null. For now, set to default timezone
  // so the event can be published. We need to ask Nylas about this. If we really need to support
  // null, we need to change our models and avro schema to reflect this.
  @Mapping(
      target = "timezone",
      defaultExpression = "java(com.UoU.core.DataConfig.Calendars.DEFAULT_TIMEZONE.getId())")
  com.UoU.infra.avro.publicevents.Recurrence toRecurrenceAvro(Recurrence.Master recurrence);

  default com.UoU.infra.avro.publicevents.RecurrenceInstance toRecurrenceInstanceAvro(
      Recurrence recurrence) {
    return Optional
        .ofNullable(recurrence)
        .flatMap(x -> x.withInstance())
        .map(this::toRecurrenceInstanceAvro)
        .orElse(null);
  }

  com.UoU.infra.avro.publicevents.RecurrenceInstance toRecurrenceInstanceAvro(
      Recurrence.Instance recurrence);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "lower")
  com.UoU.infra.avro.publicevents.EventStatus toEventStatusAvro(Event.Status status);

  @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
  List<com.UoU.infra.avro.publicevents.Participant> toParticipantsAvro(
      Iterable<Participant> participants);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "lower")
  @ValueMapping(target = "noreply", source = "NO_REPLY")
  com.UoU.infra.avro.publicevents.ParticipantStatus toParticipantStatusAvro(
      ParticipantStatus status);
}
