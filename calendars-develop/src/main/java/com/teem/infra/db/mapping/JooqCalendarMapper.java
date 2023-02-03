package com.UoU.infra.db.mapping;

import static com.UoU.infra.jooq.Tables.CALENDAR;

import com.UoU.core.calendars.Calendar;
import com.UoU.core.calendars.CalendarCreateRequest;
import com.UoU.core.calendars.CalendarUpdateRequest;
import com.UoU.core.mapping.CommonMapper;
import com.UoU.infra.jooq.tables.records.CalendarRecord;
import java.util.Map;
import org.jooq.TableField;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = JooqConfig.class)
public interface JooqCalendarMapper extends CommonMapper {

  Map<CalendarUpdateRequest.UpdateField, TableField<CalendarRecord, ?>> UPDATE_FIELDS = Map.of(
      CalendarUpdateRequest.UpdateField.NAME, CALENDAR.NAME,
      CalendarUpdateRequest.UpdateField.IS_READ_ONLY, CALENDAR.IS_READ_ONLY,
      CalendarUpdateRequest.UpdateField.TIMEZONE, CALENDAR.TIMEZONE);

  Calendar toModel(CalendarRecord record);

  @Mapping(target = "createdAt", expression = Expressions.NOW)
  @Mapping(target = "timezone", defaultExpression = Expressions.DEFAULT_TIMEZONE_ID)
  CalendarRecord toRecord(CalendarCreateRequest request);

  @Mapping(target = "updatedAt", expression = Expressions.NOW)
  CalendarRecord toRecord(CalendarUpdateRequest request);

  @AfterMapping
  static void setFieldsForUpdate(
      @MappingTarget CalendarRecord record, CalendarUpdateRequest request) {

    // Mark all updatable fields as changed based on request to only change what's needed:
    UPDATE_FIELDS.forEach((requestField, recordField) -> record.changed(
        recordField, request.hasUpdate(requestField)));

    // These fields can never change:
    record.changed(CALENDAR.ID, false);
    record.changed(CALENDAR.EXTERNAL_ID, false);
    record.changed(CALENDAR.ORG_ID, false);
    record.changed(CALENDAR.ACCOUNT_ID, false);
    record.changed(CALENDAR.CREATED_AT, false);
  }
}
