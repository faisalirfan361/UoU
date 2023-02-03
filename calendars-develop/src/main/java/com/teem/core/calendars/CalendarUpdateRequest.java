package com.UoU.core.calendars;

import com.UoU.core.OrgId;
import com.UoU.core.validation.annotations.TimeZone;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

/**
 * Request to update specific calendar fields.
 *
 * <p>Only fields in updateFields will be updated. The best way to create a request is to use
 * {@link CalendarUpdateRequest#builder()} to set fields that will automatically be added to the
 * resulting updateFields.
 *
 * <p>If you have an existing calendar object and only want to update changed fields, use
 * {@link #withMatchingUpdateFieldsRemoved(Calendar)} to remove fields that have not changed.
 */
// DO-LATER: We have several similar update request objects now, and there is some duplicate
// change tracking code and lots of similar tests. Refactor to reduce code duplication. This will
// touch some key objects, so it should probably be done in a separate ticket and release.
public record CalendarUpdateRequest(
    @NotNull @Valid CalendarId id,
    @NotNull @Valid OrgId orgId,
    @Size(max = CalendarConstraints.NAME_MAX) String name,
    Boolean isReadOnly,
    @TimeZone String timezone,
    Set<UpdateField> updateFields
) {

  public CalendarUpdateRequest {
    // Make sure update fields can't be modified, which requires copying set if mutable.
    updateFields = updateFields != null ? Set.copyOf(updateFields) : Set.of();
  }

  public boolean hasUpdates() {
    return !updateFields.isEmpty();
  }

  public boolean hasUpdate(UpdateField field) {
    return updateFields.contains(field);
  }

  public CalendarUpdateRequest withMatchingUpdateFieldsRemoved(Calendar calendar) {
    return toBuilder().removeMatchingUpdateFields(calendar).build();
  }

  public CalendarUpdateRequest.Builder toBuilder() {
    return CalendarUpdateRequest.Builder.fromInstance(this);
  }

  /**
   * Calendars fields that can be updated.
   */
  public enum UpdateField {
    NAME,
    IS_READ_ONLY,
    TIMEZONE
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder to create a CalendarUpdateRequest where each field that's set is added to updateFields.
   */
  @Getter
  public static class Builder {
    private CalendarId id;
    private OrgId orgId;
    private String name;
    private Boolean isReadOnly;
    private String timezone;

    @Getter(AccessLevel.PRIVATE)
    private final Set<UpdateField> updateFields = new HashSet<>();

    private Builder update(UpdateField field) {
      updateFields.add(field);
      return this;
    }

    public Builder id(CalendarId id) {
      this.id = id;
      return this;
    }

    public Builder orgId(OrgId orgId) {
      this.orgId = orgId;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return update(UpdateField.NAME);
    }

    public Builder isReadOnly(Boolean isReadOnly) {
      this.isReadOnly = isReadOnly;
      return update(UpdateField.IS_READ_ONLY);
    }

    public Builder timezone(String timezone) {
      this.timezone = timezone;
      return update(UpdateField.TIMEZONE);
    }

    /**
     * Removes any update fields that are equal to the corresponding calendar fields.
     *
     * <p>This is useful to create a request with only fields that would change the calendar.
     */
    public Builder removeMatchingUpdateFields(Calendar calendar) {
      removeIfEqual(UpdateField.NAME, name, calendar.name());
      removeIfEqual(UpdateField.IS_READ_ONLY, isReadOnly, calendar.isReadOnly());
      removeIfEqual(UpdateField.TIMEZONE, timezone, calendar.timezone());
      return this;
    }

    private <T> void removeIfEqual(UpdateField field, T value, T otherValue) {
      if (updateFields.contains(field) && Objects.equals(value, otherValue)) {
        updateFields.remove(field);
      }
    }

    public CalendarUpdateRequest build() {
      if (id == null) {
        throw new IllegalStateException("Build requires setting calendar id");
      }

      return new CalendarUpdateRequest(id, orgId, name, isReadOnly, timezone, updateFields);
    }

    public static Builder fromInstance(CalendarUpdateRequest instance) {
      val builder = builder();
      builder.id = instance.id;
      builder.orgId = instance.orgId;
      builder.name = instance.name;
      builder.isReadOnly = instance.isReadOnly;
      builder.timezone = instance.timezone;
      builder.updateFields.addAll(instance.updateFields);
      return builder;
    }
  }
}
