package com.UoU.core.events;

import com.UoU.core.OrgId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

/**
 * Request to update specific event fields.
 *
 * <p>Only fields in updateFields will be updated. The best way to create a request is to use
 * {@link EventUpdateRequest#builder()} to set fields that will automatically be added to the
 * resulting updateFields.
 *
 * <p>If you have an existing event object and only want to update changed fields, use
 * {@link #withMatchingUpdateFieldsRemoved(Event)} to remove fields that have not changed.
 */
public record EventUpdateRequest(
    @Valid @NotNull EventId id,
    @Valid EventExternalId externalId,
    String icalUid,
    @Valid @NotNull OrgId orgId,
    @Size(min = 1, max = EventConstraints.TITLE_MAX) String title, // Nylas requires min 1 char
    @Size(max = EventConstraints.DESCRIPTION_MAX) String description,
    @Size(max = EventConstraints.LOCATION_MAX) String location,
    @Valid @NotNull When when,
    @Valid Recurrence.Master recurrence, // can only update master info, not instances
    Event.Status status,
    Boolean isBusy,
    Boolean isReadOnly,
    @Valid Owner owner,
    @Valid List<ParticipantRequest> participants,
    Set<UpdateField> updateFields,
    @Valid DataSource dataSource
) {

  public EventUpdateRequest {
    // Make sure update fields can't be modified, which requires copying set if mutable.
    updateFields = updateFields != null ? Set.copyOf(updateFields) : Set.of();
  }

  public boolean hasUpdates() {
    return !updateFields.isEmpty();
  }

  public boolean hasUpdate(UpdateField field) {
    return updateFields.contains(field);
  }

  public EventUpdateRequest withUpdateFieldsRemoved(UpdateField... updateFieldsToRemove) {
    return toBuilder().removeUpdateFields(updateFieldsToRemove).build();
  }

  public EventUpdateRequest withMatchingUpdateFieldsRemoved(Event event) {
    return toBuilder().removeMatchingUpdateFields(event).build();
  }

  public Builder toBuilder() {
    return Builder.fromInstance(this);
  }

  /**
   * Fields that can be updated via EventUpdateRequest.
   */
  public enum UpdateField {
    EXTERNAL_ID,
    ICAL_UID,
    TITLE,
    DESCRIPTION,
    LOCATION,
    WHEN,
    RECURRENCE,
    STATUS,
    IS_BUSY,
    IS_READ_ONLY,
    OWNER,
    PARTICIPANTS
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to create an EventUpdateRequest where each field that's set is added to updateFields.
   */
  @Getter
  public static class Builder {
    private EventId id;
    private EventExternalId externalId;
    private String icalUid;
    private OrgId orgId;
    private String title;
    private String description;
    private String location;
    private When when;
    private Recurrence.Master recurrence;
    private Event.Status status;
    private Boolean isBusy;
    private Boolean isReadOnly;
    private Owner owner;
    private List<ParticipantRequest> participants;
    private DataSource dataSource;

    @Getter(AccessLevel.PRIVATE) private final Set<UpdateField> updateFields = new HashSet<>();

    private Builder update(UpdateField field) {
      updateFields.add(field);
      return this;
    }

    public Builder id(EventId id) {
      this.id = id;
      return this;
    }

    public Builder externalId(EventExternalId externalId) {
      this.externalId = externalId;
      return update(UpdateField.EXTERNAL_ID);
    }

    public Builder icalUid(String icalUid) {
      this.icalUid = icalUid;
      return update(UpdateField.ICAL_UID);
    }

    public Builder orgId(OrgId orgId) {
      this.orgId = orgId;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return update(UpdateField.TITLE);
    }

    public Builder description(String description) {
      this.description = description;
      return update(UpdateField.DESCRIPTION);
    }

    public Builder location(String location) {
      this.location = location;
      return update(UpdateField.LOCATION);
    }

    public Builder when(When when) {
      this.when = when;
      return update(UpdateField.WHEN);
    }

    public Builder recurrence(Recurrence.Master recurrence) {
      this.recurrence = recurrence;
      return update(UpdateField.RECURRENCE);
    }

    public Builder status(Event.Status status) {
      this.status = status;
      return update(UpdateField.STATUS);
    }

    public Builder isBusy(boolean isBusy) {
      this.isBusy = isBusy;
      return update(UpdateField.IS_BUSY);
    }

    public Builder isReadOnly(boolean isReadOnly) {
      this.isReadOnly = isReadOnly;
      return update(UpdateField.IS_READ_ONLY);
    }

    public Builder owner(Owner owner) {
      this.owner = owner;
      return update(UpdateField.OWNER);
    }

    public Builder participants(List<ParticipantRequest> participants) {
      this.participants = participants;
      return update(UpdateField.PARTICIPANTS);
    }

    /**
     * Sets the dataSource for the update (e.g. web, mobile, etc.)
     *
     * <p>This field is NOT an UpdateField and will only be updated if other event data is updated,
     * since this is metadata about the update itself.
     */
    public Builder dataSource(DataSource dataSource) {
      this.dataSource = dataSource;
      return this;
    }

    public Builder removeUpdateFields(UpdateField... updateFieldsToRemove) {
      Arrays.stream(updateFieldsToRemove).forEach(updateFields::remove);
      return this;
    }

    /**
     * Removes any update fields that are equal to the corresponding event fields.
     *
     * <p>This is useful to create an update request with only fields that would change the event.
     */
    public Builder removeMatchingUpdateFields(Event event) {
      removeIfEqual(UpdateField.EXTERNAL_ID, externalId, event.externalId());
      removeIfEqual(UpdateField.ICAL_UID, icalUid, event.icalUid());
      removeIfEqual(UpdateField.TITLE, title, event.title());
      removeIfEqual(UpdateField.DESCRIPTION, description, event.description());
      removeIfEqual(UpdateField.LOCATION, location, event.location());
      removeIfEqual(UpdateField.WHEN, when, event.when());
      removeIfEqual(UpdateField.STATUS, status, event.status());
      removeIfEqual(UpdateField.IS_BUSY, isBusy, event.isBusy());
      removeIfEqual(UpdateField.IS_READ_ONLY, isReadOnly, event.isReadOnly());
      removeIfEqual(UpdateField.OWNER, owner, event.owner());

      // All-day event Whens are a special case because the When object may or may not have the
      // read-only effectiveUtcTimeSpan property calculated. If the Whens are the same except only
      // one has effectiveUtcTimeSpan, consider these equal and the When unchanged.
      if (updateFields.contains(UpdateField.WHEN)
          && when != null && event.when() != null
          && when.isAllDay() && event.when().isAllDay()
          && when.type().equals(event.when().type())
          && when.effectiveUtcTimeSpan().isEmpty() != event.when().effectiveUtcTimeSpan().isEmpty()
          && when.toAllDayDateSpan().equals(event.when().toAllDayDateSpan())) {
        updateFields.remove(UpdateField.WHEN);
      }

      // Recurrence master should be compared with dataEquals() to ignore non-event-data properties.
      if (updateFields.contains(UpdateField.RECURRENCE)
          && ((recurrence == null && !event.recurrence().isMaster())
          || (recurrence != null && recurrence.dataEquals(event.recurrence().getMaster())))) {
        updateFields.remove(UpdateField.RECURRENCE);
      }

      // ParticipantRequests also have change tracking so use inner withMatchingUpdateFieldsRemoved.
      // If event participants and request participants are the same size, and there are no field
      // updates, we can safely unset the participants from being updated at all.
      if (updateFields.contains(UpdateField.PARTICIPANTS)) {
        val eventParticipants = Optional.ofNullable(event.participants()).orElse(List.of());
        participants = ParticipantRequest.withMatchingUpdateFieldsRemoved(
            participants, eventParticipants);

        if (eventParticipants.size() == participants.size()
            && participants.stream().allMatch(x -> !x.hasUpdates())) {
          updateFields.remove(UpdateField.PARTICIPANTS);
        }
      }

      return this;
    }

    /**
     * Removes the update field if value equals otherValue via Object.equals().
     */
    private <T> void removeIfEqual(UpdateField field, T value, T otherValue) {
      if (updateFields.contains(field) && Objects.equals(value, otherValue)) {
        updateFields.remove(field);
      }
    }

    public EventUpdateRequest build() {
      if (id == null) {
        throw new IllegalStateException("Build requires setting event id");
      }

      return new EventUpdateRequest(id, externalId, icalUid, orgId, title, description, location,
          when, recurrence, status, isBusy, isReadOnly, owner, participants, updateFields,
          dataSource);
    }

    public static Builder fromInstance(EventUpdateRequest instance) {
      val builder = builder();
      builder.id = instance.id;
      builder.externalId = instance.externalId;
      builder.icalUid = instance.icalUid;
      builder.orgId = instance.orgId;
      builder.title = instance.title;
      builder.description = instance.description;
      builder.location = instance.location;
      builder.when = instance.when;
      builder.recurrence = instance.recurrence;
      builder.status = instance.status;
      builder.isBusy = instance.isBusy;
      builder.isReadOnly = instance.isReadOnly;
      builder.owner = instance.owner;
      builder.participants = instance.participants;
      builder.updateFields.addAll(instance.updateFields);
      builder.dataSource(instance.dataSource);
      return builder;
    }
  }
}
