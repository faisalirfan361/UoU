package com.UoU.core.nylas.mapping;

import com.UoU.core.OrgId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.events.EventCreateRequest;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventUpdateRequest;
import com.UoU.core.events.Owner;
import com.UoU.core.events.Participant;
import com.UoU.core.events.Recurrence;
import com.UoU.core.events.When;
import com.UoU.core.nylas.RecurrenceInfo;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = NylasConfig.class, uses = NylasParticipantMapper.class)
public interface NylasEventMapper {

  /**
   * Matches owner name/email strings like: Someone &lt;someone@example.com&gt;
   */
  Pattern OWNER_PATTERN = Pattern.compile("^\s*(.+)?\s*<([^>]+@[^>]+)>\s*$");

  String METADATA_KEY_CHECKIN_AT = "checkinAt";
  String METADATA_KEY_CHECKOUT_AT = "checkoutAt";

  default EventCreateRequest toCreateRequestModel(
      com.nylas.Event event, EventId id, CalendarId calendarId, OrgId orgId) {
    return toCreateRequestModel(event, id, null, calendarId, orgId);
  }

  @Mapping(target = "id", source = "id")
  @Mapping(target = "externalId", source = "event.id")
  @Mapping(target = "icalUid", source = "event.icalUid")
  @Mapping(target = "orgId", source = "orgId")
  @Mapping(target = "calendarId", source = "calendarId")
  @Mapping(target = "title", source = "event.title")
  @Mapping(target = "description", source = "event.description")
  @Mapping(target = "location", source = "event.location")
  @Mapping(target = "when", qualifiedByName = "toWhenModel")
  @Mapping(target = "recurrence", source = "event", qualifiedByName = "toRecurrenceModel")
  @Mapping(target = "isBusy", source = "event.busy")
  @Mapping(target = "isReadOnly", source = "event.readOnly")
  @Mapping(target = "checkinAt", source = "event.metadata", qualifiedByName = "toCheckinAt")
  @Mapping(target = "checkoutAt", source = "event.metadata", qualifiedByName = "toCheckoutAt")
  @Mapping(target = "dataSource", expression = Expressions.DATA_SOURCE_PROVIDER)
  EventCreateRequest toCreateRequestModel(
      com.nylas.Event event, EventId id, @Context EventId recurrenceMasterId,
      CalendarId calendarId, OrgId orgId);

  @Mapping(target = "id", source = "localEvent.id")
  @Mapping(target = "externalId", source = "event.id")
  @Mapping(target = "orgId", source = "localEvent.orgId")
  @Mapping(target = "icalUid", source = "event.icalUid")
  @Mapping(target = "title", source = "event.title")
  @Mapping(target = "description", source = "event.description")
  @Mapping(target = "location", source = "event.location")
  @Mapping(target = "when", source = "event.when", qualifiedByName = "toWhenModel")
  @Mapping(target = "recurrence", source = "event.recurrence",
      conditionExpression = "java(event.getMasterEventId() == null)")
  @Mapping(target = "status", source = "event.status")
  @Mapping(target = "isBusy", source = "event.busy")
  @Mapping(target = "isReadOnly", source = "event.readOnly")
  @Mapping(target = "owner", source = "event.owner")
  @Mapping(target = "participants", source = "event.participants")
  @Mapping(target = "dataSource", expression = Expressions.DATA_SOURCE_PROVIDER)
  EventUpdateRequest toUpdateRequestModel(
      com.nylas.Event event, com.UoU.core.events.Event localEvent);

  @AfterMapping
  static void afterToUpdateRequestModel(
      @MappingTarget EventUpdateRequest.Builder builder, com.UoU.core.events.Event localEvent) {

    // If event is a recurrence instance, make sure the recurrence master field is not set. For
    // MS/Google, recurrence field will be null for instances anyway, but for virtual calendars,
    // Nylas returns the recurrence field with RRULE on both master and instances (which is weird).
    // Mappings should avoid setting the recurrence field when invalid, but this is extra safety.
    if (localEvent.recurrence().isInstance()) {
      builder.recurrence(null).removeUpdateFields(EventUpdateRequest.UpdateField.RECURRENCE);
    }

    // When mapping from local event, we can unset fields that haven't changed for minimum update:
    builder.removeMatchingUpdateFields(localEvent);
  }

  @Named("toWhenModel")
  default When toWhenModel(com.nylas.Event.When when) {
    if (when instanceof com.nylas.Event.Timespan nylasWhen) {
      return new When.TimeSpan(
          nylasWhen.getStartTime(),
          nylasWhen.getEndTime());
    } else if (when instanceof com.nylas.Event.Datespan nylasWhen) {
      return new When.DateSpan(nylasWhen.getStartDate(), nylasWhen.getEndDate());
    } else if (when instanceof com.nylas.Event.Date nylasWhen) {
      return new When.Date(nylasWhen.getDate());
    } else {
      throw new IllegalArgumentException("Unknown 'when' type in Nylas event: "
          + when.getClass().getName());
    }
  }

  @Named("toCheckinAt")
  default Instant toCheckinAt(Map<String, String> metadata) {
    return mapFromMetadata(
        metadata, METADATA_KEY_CHECKIN_AT,
        value -> value.filter(x -> !x.isBlank()).map(Instant::parse));
  }

  @Named("toCheckoutAt")
  default Instant toCheckoutAt(Map<String, String> metadata) {
    return mapFromMetadata(
        metadata, METADATA_KEY_CHECKOUT_AT,
        value -> value.filter(x -> !x.isBlank()).map(Instant::parse));
  }

  private static <T> T mapFromMetadata(
      Map<String, String> metadata,
      String key,
      Function<Optional<String>, Optional<T>> mapper) {
    return mapper.apply(Optional.ofNullable(metadata).map(x -> x.get(key))).orElse(null);
  }

  default Owner toOwnerModelFromNylasString(String owner) {
    if (owner == null || owner.isBlank()) {
      return null;
    }

    // Find expected pattern like: Some Person <some.person@example.com>
    val matches = OWNER_PATTERN.matcher(owner);
    if (matches.matches()) {
      val name = StringUtils.trimToNull(matches.group(1));
      val email = StringUtils.trimToNull(matches.group(2));
      return new Owner(name, email);
    }

    // If no match, but string contains @, assume an email and use that.
    if (owner.contains("@")) {
      return new Owner(null, owner.trim());
    }

    return null;
  }

  default com.UoU.core.events.Event.Status toStatusModel(String status) {
    return status == null ? null : com.UoU.core.events.Event.Status.byStringValue(status);
  }

  @Named("toRecurrenceModel")
  default Recurrence toRecurrenceModel(com.nylas.Event event, @Context EventId recurrenceMasterId) {
    return Optional
        .ofNullable(recurrenceMasterId)
        .flatMap(masterId -> Optional
            .of(new RecurrenceInfo(event))
            .filter(x -> x.isInstance())
            .map(x -> Recurrence.instance(masterId, x.isOverrideInstance())))
        .or(() -> Optional.ofNullable(event)
            .map(com.nylas.Event::getRecurrence)
            .map(this::toRecurrenceMasterModel)
            .map(Recurrence::master))
        .orElse(Recurrence.none());
  }

  Recurrence.Master toRecurrenceMasterModel(com.nylas.Event.Recurrence recurrence);

  default com.nylas.Event toNylasEvent(
      com.UoU.core.events.Event event, CalendarExternalId calendarExternalId) {
    val nylasEvent = new com.nylas.Event(calendarExternalId.value(), toNylasWhen(event.when()));
    updateNylasEvent(nylasEvent, event);

    return nylasEvent;
  }

  void updateNylasEvent(
      @MappingTarget com.nylas.Event nylasEvent, com.UoU.core.events.Event event);

  /**
   * Sets a few things that can't be done (easily) within {@link #updateNylasEvent}.
   *
   * <p>This must be public for MapStruct, but DO NOT USER OUTSIDE THIS MAPPER!
   */
  @AfterMapping
  default void afterUpdateNylasEvent(
      @MappingTarget com.nylas.Event nylasEvent,
      com.UoU.core.events.Event event) {

    // Add checkin/checkout metadata to existing metadata (without clearing out existing).
    // Except this metadata doesn't make sense for recurrence master events because you can't
    // checkin/checkout from an entire series, just particular instances. Also metadata isn't
    // supported for auto-created (non-override) recurrence instances; instead, you have to create
    // the override instance, get the new id, then save the metadata with the new id. We can't
    // currently do anything about this Nylas limitation, so auto-created instances won't have
    // metadata synced. For these non-override instances, we have to set the metadata to null
    // or else the API will throw an error that metadata is not supported.
    val isRecurrenceNonOverrideInstance = event.recurrence().isInstanceThat(x -> x.isNotOverride());
    if (!(isRecurrenceNonOverrideInstance || event.recurrence().isMaster())) {
      nylasEvent.addMetadata(METADATA_KEY_CHECKIN_AT, toMetadataString(event.checkinAt()));
      nylasEvent.addMetadata(METADATA_KEY_CHECKOUT_AT, toMetadataString(event.checkoutAt()));
    } else if (isRecurrenceNonOverrideInstance) {
      nylasEvent.setMetadata(null); // set null to avoid API error
    }

    // Notifications are not supported for recurrence masters or non-override instances, so set null
    // to avoid API error: "RequestFailedException[message=Enhanced events are not supported with
    // event override.]" The error does not make much sense, since it's non-overrides that fail.
    if (isRecurrenceNonOverrideInstance || event.recurrence().isMaster()) {
      nylasEvent.setNotifications(null);
    }

    // DO-MAYBE: Update the conferencing field value based on the provider type.
    // For now, we're deliberately setting the conferencing value to null, so that when it's passed
    // to the Nylas API, we avoid getting an error that states "Updating `conferencing` is only
    // supported for events that were created through the API."
    // This seems to be a limitation for only Microsoft Exchange. The Nylas docs state
    // the conferencing field is read-only for Exchange, but full CRUD is available for Google.
    // https://developer.nylas.com/docs/connectivity/calendar/manually-create-meetings
    nylasEvent.setConferencing(null);
  }

  private String toMetadataString(Object value) {
    return Optional.ofNullable(value).map(x -> x.toString()).orElse("");
  }

  default List<com.nylas.Participant> toNylasParticipants(List<Participant> participants) {
    if (participants == null || participants.isEmpty()) {
      return null;
    }

    // Only map participant name and email. Status and comment can be set on new events, but they'll
    // be reset by the provider and will cause an unnecessary update, so don't set from our side.
    return participants.stream()
        .map(x -> new com.nylas.Participant(x.email()).name(x.name()))
        .toList();
  }

  default com.nylas.Event.When toNylasWhen(When when) {
    switch (when.type()) {
      case TIMESPAN -> {
        return new com.nylas.Event.Timespan(
            ((When.TimeSpan) when).startTime(),
            ((When.TimeSpan) when).endTime());
      }
      case DATESPAN -> {
        return new com.nylas.Event.Datespan(
            ((When.DateSpan) when).startDate(),
            ((When.DateSpan) when).endDate());
      }
      case DATE -> {
        return new com.nylas.Event.Date(((When.Date) when).date());
      }
      default -> throw new IllegalArgumentException("Unknown 'when' type in event: "
          + when.getClass().getName());
    }
  }

  default com.nylas.Event.Recurrence toNylasRecurrence(Recurrence recurrence) {
    return Optional
        .ofNullable(recurrence)
        .map(Recurrence::getMaster)
        .map(x -> new com.nylas.Event.Recurrence(x.timezone(), x.rrule()))
        .orElse(null);
  }
}

