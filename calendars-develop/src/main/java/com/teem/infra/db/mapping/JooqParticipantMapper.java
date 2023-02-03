package com.UoU.infra.db.mapping;

import static com.UoU.infra.jooq.Tables.PARTICIPANT;

import com.UoU.core.events.EventId;
import com.UoU.core.events.ParticipantRequest;
import com.UoU.infra.jooq.enums.ParticipantStatus;
import com.UoU.infra.jooq.tables.records.ParticipantRecord;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.jooq.TableField;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ValueMapping;

@Mapper(config = JooqConfig.class)
public interface JooqParticipantMapper {

  Map<ParticipantRequest.UpdateField, TableField<ParticipantRecord, ?>> PARTICIPANT_UPDATE_FIELDS =
      Map.of(
          ParticipantRequest.UpdateField.NAME, PARTICIPANT.NAME,
          ParticipantRequest.UpdateField.STATUS, PARTICIPANT.STATUS,
          ParticipantRequest.UpdateField.COMMENT, PARTICIPANT.COMMENT);

  /**
   * Creates new record for insert, which means all fields will be marked as set/changed.
   */
  @Mapping(target = "status", defaultValue = "noreply")
  ParticipantRecord toRecordForCreate(ParticipantRequest participant, EventId eventId);

  /**
   * Helper to call {@link #toRecordForCreate(ParticipantRequest, EventId)} with a list.
   */
  default List<ParticipantRecord> toRecordsForCreate(
      List<ParticipantRequest> participants, EventId eventId) {
    return participants == null
        ? List.of()
        : participants.stream().map(x -> toRecordForCreate(x, eventId)).toList();
  }

  /**
   * Updates an existing ParticipantRecord with changes from a ParticipantRequest.
   *
   * <p>{@link #afterUpdateRecord(ParticipantRecord, ParticipantRequest)} will be called afterward
   * to finish the update.
   */
  @BeanMapping(qualifiedByName = "afterUpdateRecord")
  @Mapping(target = "email", ignore = true) // never update email because it's part of key
  void updateRecord(@MappingTarget ParticipantRecord record, ParticipantRequest participant);

  /**
   * For mapstruct to call after {@link #updateRecord(ParticipantRecord, ParticipantRequest)}.
   *
   * <p>DON'T CALL THIS DIRECTLY!
   */
  @AfterMapping
  @Named("afterUpdateRecord")
  static void afterUpdateRecord(
      @MappingTarget ParticipantRecord record, ParticipantRequest request) {

    // Mark all updatable fields as changed based on request to only change what's needed:
    PARTICIPANT_UPDATE_FIELDS.forEach((field, recordField) -> {
      val isChanged = request.updateFields().contains(field);
      record.changed(recordField, isChanged);
    });

    // Ensure some core fields are never changed because they can only be set on creation:
    record.changed(PARTICIPANT.EVENT_ID, false);
    record.changed(PARTICIPANT.EMAIL, false);
  }

  @EnumMapping(nameTransformationStrategy = "case", configuration = "lower")
  @ValueMapping(target = "noreply", source = "NO_REPLY")
  ParticipantStatus mapStatus(com.UoU.core.events.ParticipantStatus status);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
  @ValueMapping(target = "NO_REPLY", source = "noreply")
  com.UoU.core.events.ParticipantStatus mapStatus(ParticipantStatus status);
}
