package com.UoU.infra.db.mapping;

import static com.UoU.core.events.EventUpdateRequest.UpdateField;
import static com.UoU.infra.jooq.Tables.EVENT;
import static java.util.Map.entry;
import static org.jooq.JSON.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.UoU.core.TimeSpan;
import com.UoU.core.events.DataSource;
import com.UoU.core.events.Event;
import com.UoU.core.events.EventCreateRequest;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventUpdateRequest;
import com.UoU.core.events.Owner;
import com.UoU.core.events.Recurrence;
import com.UoU.core.events.When;
import com.UoU.core.mapping.CommonMapper;
import com.UoU.infra.jooq.enums.EventStatus;
import com.UoU.infra.jooq.tables.records.EventRecord;
import com.UoU.infra.jooq.tables.records.ParticipantRecord;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.val;
import org.jooq.JSON;
import org.jooq.TableField;
import org.mapstruct.AfterMapping;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ValueMapping;

@Mapper(config = JooqConfig.class, uses = {JooqParticipantMapper.class})
public interface JooqEventMapper extends CommonMapper {
  ObjectMapper MAPPER = new ObjectMapper();

  Map<UpdateField, List<TableField<EventRecord, ?>>> EVENT_UPDATE_FIELDS =
      Map.ofEntries(
          entry(UpdateField.EXTERNAL_ID, List.of(EVENT.EXTERNAL_ID)),
          entry(UpdateField.ICAL_UID, List.of(EVENT.ICAL_UID)),
          entry(UpdateField.TITLE, List.of(EVENT.TITLE)),
          entry(UpdateField.DESCRIPTION, List.of(EVENT.DESCRIPTION)),
          entry(UpdateField.LOCATION, List.of(EVENT.LOCATION)),
          entry(UpdateField.WHEN, List.of(
              EVENT.START_AT, EVENT.END_AT,
              EVENT.IS_ALL_DAY, EVENT.ALL_DAY_START_AT, EVENT.ALL_DAY_END_AT)),
          entry(UpdateField.RECURRENCE, List.of(EVENT.RECURRENCE)),
          entry(UpdateField.STATUS, List.of(EVENT.STATUS)),
          entry(UpdateField.IS_BUSY, List.of(EVENT.IS_BUSY)),
          entry(UpdateField.IS_READ_ONLY, List.of(EVENT.IS_READ_ONLY)),
          entry(UpdateField.OWNER, List.of(EVENT.OWNER_NAME, EVENT.OWNER_EMAIL)));

  default Event toModel(EventRecord record) {
    return toModel(record, null);
  }

  @Mapping(target = "when", expression = "java(mapWhen(record))")
  @Mapping(target = "recurrence", source = "record", qualifiedByName = "mapRecordToRecurrence")
  @Mapping(target = "owner", source = "record")
  Event toModel(EventRecord record, List<ParticipantRecord> participants);

  /**
   * Maps record status to model status.
   *
   * <p>Note that "cancelled" is a valid Nylas status but won't occur locally because we delete
   * cancelled events. The db still allows cancelled for later flexibility, but app code doesn't.
   */
  @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
  @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = "cancelled")
  Event.Status mapStatusToModel(EventStatus status);

  /**
   * Maps model status to record status.
   *
   * <p>Note that "cancelled" is a valid Nylas status but won't occur locally because we delete
   * cancelled events. The db still allows cancelled for later flexibility, but app code doesn't.
   */
  @EnumMapping(nameTransformationStrategy = "case", configuration = "lower")
  EventStatus mapStatusToRecord(Event.Status status);

  default Owner mapRecordToOwner(EventRecord record) {
    return record != null && (record.getOwnerName() != null || record.getOwnerEmail() != null)
        ? new Owner(record.getOwnerName(), record.getOwnerEmail())
        : null;
  }

  @SneakyThrows
  @Named("mapRecordToRecurrence")
  default Recurrence mapRecordToRecurrence(EventRecord record) {
    return Optional
        .ofNullable(record)
        .map(x -> mapToRecurrence(
            x.getRecurrence(),
            x.getRecurrenceMasterId(),
            x.getIsRecurrenceOverride()))
        .orElse(Recurrence.none());
  }

  @SneakyThrows
  default Recurrence mapToRecurrence(
      JSON recurrence, UUID recurrenceMasterId, boolean isRecurrenceOverride) {

    if (recurrenceMasterId != null) {
      return Recurrence.instance(new EventId(recurrenceMasterId), isRecurrenceOverride);
    } else if (recurrence != null) {
      return Recurrence.master(MAPPER.readValue(recurrence.toString(), Recurrence.Master.class));
    }
    return Recurrence.none();
  }

  @SneakyThrows
  @Named("mapRecurrrenceToJson")
  default JSON mapRecurrrenceToJson(Recurrence.Master master) {
    return master == null ? null : json(MAPPER.writeValueAsString(master));
  }

  @Mapping(target = "id", source = "request.id")
  @Mapping(target = "recurrence", source = "request.recurrence.master",
      qualifiedByName = "mapRecurrrenceToJson")
  @Mapping(target = "recurrenceMasterId", source = "request.recurrence.instance.masterId")
  @Mapping(target = "isRecurrenceOverride", source = "request.recurrence.instance.isOverride",
      defaultValue = "false")
  @Mapping(target = "ownerName", source = "request.owner.name")
  @Mapping(target = "ownerEmail", source = "request.owner.email")
  @Mapping(target = "createdAt", expression = Expressions.NOW)
  @Mapping(target = "createdFrom", source = "request.dataSource")
  EventRecord toRecord(EventCreateRequest request, Supplier<ZoneId> zoneSupplier);

  @Mapping(target = "id", source = "request.id")
  @Mapping(target = "orgId", ignore = true)
  @Mapping(target = "recurrence", qualifiedByName = "mapRecurrrenceToJson")
  @Mapping(target = "ownerName", source = "request.owner.name")
  @Mapping(target = "ownerEmail", source = "request.owner.email")
  @Mapping(target = "updatedAt", expression = Expressions.NOW)
  @Mapping(target = "updatedFrom", source = "request.dataSource")
  EventRecord toRecord(EventUpdateRequest request, Supplier<ZoneId> zoneSupplier);

  @AfterMapping
  static void afterToRecordForCreate(
      @MappingTarget EventRecord record,
      EventCreateRequest request,
      Supplier<ZoneId> zoneSupplier) {

    if (record.getId() == null) {
      throw new IllegalArgumentException("Event record id cannot be null");
    }

    setTimeFields(record, request.when(), zoneSupplier);
  }

  @AfterMapping
  static void afterToRecordForUpdate(
      @MappingTarget EventRecord record,
      EventUpdateRequest request,
      Supplier<ZoneId> zoneSupplier) {

    if (record.getId() == null) {
      throw new IllegalArgumentException("Event record id cannot be null");
    }

    // Mark all updatable fields as changed based on request to only change what's needed:
    EVENT_UPDATE_FIELDS.forEach((field, recordFields) -> {
      val isChanged = request.updateFields().contains(field);
      recordFields.forEach(recordField -> record.changed(recordField, isChanged));
    });

    // Update time fields only if WHEN is requested to update:
    if (request.updateFields().contains(UpdateField.WHEN)) {
      setTimeFields(record, request.when(), zoneSupplier);
    }

    // Ensure some core fields are never changed because they can only be set on creation:
    record.changed(EVENT.ID, false);
    record.changed(EVENT.ORG_ID, false);
    record.changed(EVENT.CALENDAR_ID, false);
    record.changed(EVENT.CREATED_AT, false);
    record.changed(EVENT.CREATED_FROM, false);
    record.changed(EVENT.RECURRENCE_MASTER_ID, false);
    record.changed(EVENT.IS_RECURRENCE_OVERRIDE, false);
  }

  default When mapWhen(EventRecord record) {
    if (record.getIsAllDay()) {
      val effectiveUtcTimeSpan = new TimeSpan(record.getStartAt(), record.getEndAt());
      if (record.getAllDayStartAt().isEqual(record.getAllDayEndAt())) {
        return new When.Date(
            record.getAllDayStartAt(),
            effectiveUtcTimeSpan);
      } else {
        return new When.DateSpan(
            record.getAllDayStartAt(),
            record.getAllDayEndAt(),
            effectiveUtcTimeSpan);
      }
    } else {
      return new When.TimeSpan(
          mapToInstant(record.getStartAt()),
          mapToInstant(record.getEndAt()));
    }
  }

  default DataSource toDataSourceModel(String dataSource) {
    return new DataSource(dataSource);
  }

  private static void setTimeFields(
      EventRecord eventRecord, When when, Supplier<ZoneId> zoneSupplier) {

    val utcTimeSpan = when
        .effectiveUtcTimeSpan() // will usually be empty for creates/updates, but use if it exists.
        .orElseGet(() -> when.toUtcTimeSpan(zoneSupplier));
    eventRecord.setStartAt(utcTimeSpan.startAtUtcOffset());
    eventRecord.setEndAt(utcTimeSpan.endAtUtcOffset());

    when.toAllDayDateSpan().ifPresentOrElse(
        dateSpan -> eventRecord
            .setIsAllDay(true)
            .setAllDayStartAt(dateSpan.startDate())
            .setAllDayEndAt(dateSpan.endDate()),
        () -> eventRecord
            .setIsAllDay(false)
            .setAllDayStartAt(null)
            .setAllDayEndAt(null));
  }
}
