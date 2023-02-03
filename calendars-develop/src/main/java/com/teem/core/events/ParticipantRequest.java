package com.UoU.core.events;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

/**
 * Request to create or update a participant, which should be part of an event create or update.
 *
 * <p>For updates only, {@link #updateFields} allows you to specify which fields need to be
 * updated, which is useful if you want to update some fields but don't know the existing values
 * of others. For example, in API updates, only name and email are updated, while other fields
 * are expected to be preserved. For creates, updateFields will be ignored, since all fields have
 * to be set even if the value is null.
 *
 * <p>The best way to create a request is to use {@link ParticipantRequest#builder()} to set fields
 * that will automatically be added to the resulting updateFields.
 *
 * <p>If you have an existing participant object and only want to update changed fields, use
 * {@link #withMatchingUpdateFieldsRemoved(Participant)} to remove fields that have not changed.
 */
public record ParticipantRequest(
    @Size(max = EventConstraints.PARTICIPANT_NAME_MAX) String name,
    @NotEmpty @Email String email,
    ParticipantStatus status,
    @Size(max = EventConstraints.PARTICIPANT_COMMENT_MAX) String comment,
    Set<UpdateField> updateFields
) {

  public ParticipantRequest {
    // Make sure update fields can't be modified, which requires copying set if mutable.
    updateFields = updateFields != null ? Set.copyOf(updateFields) : Set.of();
  }

  public boolean hasUpdates() {
    return !updateFields.isEmpty();
  }

  public ParticipantRequest withMatchingUpdateFieldsRemoved(Participant participant) {
    return toBuilder().removeMatchingUpdateFields(participant).build();
  }

  /**
   * Helper that calls {@link #withMatchingUpdateFieldsRemoved(Participant)} with lists.
   */
  public static List<ParticipantRequest> withMatchingUpdateFieldsRemoved(
      List<ParticipantRequest> requests,
      List<Participant> participants) {

    val oldByEmail = Optional
        .ofNullable(participants)
        .map(list -> list.stream().collect(Collectors.toMap(x -> x.email(), x -> x)))
        .orElse(Map.of());

    return Optional
        .ofNullable(requests)
        .map(x -> x.stream())
        .orElse(Stream.of())
        .map(participant -> Optional
            .ofNullable(oldByEmail.get(participant.email()))
            .map(participant::withMatchingUpdateFieldsRemoved)
            .orElse(participant))
        .toList();
  }

  public Builder toBuilder() {
    return Builder.fromInstance(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Fields that can be updated for existing participants.
   */
  public enum UpdateField {
    NAME,
    STATUS,
    COMMENT,
  }

  /**
   * Builder to create a ParticipantRequest where each field that's set is added to updateFields.
   */
  @Getter
  public static class Builder {
    private String name;
    private String email;
    private ParticipantStatus status;
    private String comment;

    @Getter(AccessLevel.PRIVATE)
    private final Set<UpdateField> updateFields = new HashSet<>();

    private Builder update(UpdateField field) {
      updateFields.add(field);
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return update(UpdateField.NAME);
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder status(ParticipantStatus status) {
      this.status = status;
      return update(UpdateField.STATUS);
    }

    public Builder comment(String comment) {
      this.comment = comment;
      return update(UpdateField.COMMENT);
    }

    /**
     * Removes any updateFields that are equal to the corresponding participant fields.
     *
     * <p>This is useful to create an update request with only the fields that need to change.
     */
    public Builder removeMatchingUpdateFields(Participant participant) {
      removeIfEqual(UpdateField.NAME, name, participant.name());
      removeIfEqual(UpdateField.STATUS, status, participant.status());
      removeIfEqual(UpdateField.COMMENT, comment, participant.comment());
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

    public ParticipantRequest build() {
      return new ParticipantRequest(name, email, status, comment, updateFields);
    }

    /**
     * Creates a builder from an existing instance, copying all values to the builder.
     */
    public static Builder fromInstance(ParticipantRequest instance) {
      val builder = new Builder();
      builder.email = instance.email;
      builder.name = instance.name;
      builder.status = instance.status;
      builder.comment = instance.comment;
      builder.updateFields.addAll(instance.updateFields);
      return builder;
    }
  }
}
