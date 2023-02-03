package com.UoU.core.mapping;

import com.UoU.core.OrgId;
import com.UoU.core.SecretString;
import com.UoU.core.WrappedValue;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.calendars.CalendarExternalId;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.conferencing.ConferencingUserId;
import com.UoU.core.events.EventExternalId;
import com.UoU.core.events.EventId;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.springframework.lang.Nullable;

/**
 * Mapper for wrapped values that need conversion in many different contexts.
 */
@Mapper(config = Config.class)
public interface WrappedValueMapper {

  @Nullable
  default <T> T toValue(WrappedValue<T> wrapper) {
    return wrapper == null ? null : wrapper.value();
  }

  @Nullable
  default OrgId toOrgId(String value) {
    return value == null ? null : new OrgId(value);
  }

  @Nullable
  default AccountId toAccountId(String value) {
    return value == null ? null : new AccountId(value);
  }

  @Nullable
  default ServiceAccountId toServiceAccountId(UUID value) {
    return value == null ? null : new ServiceAccountId(value);
  }

  @Nullable
  default CalendarId toCalendarId(String value) {
    return value == null ? null : new CalendarId(value);
  }

  @Nullable
  default CalendarExternalId toCalendarExternalId(String value) {
    return value == null ? null : new CalendarExternalId(value);
  }

  @Nullable
  default EventId toEventId(UUID value) {
    return value == null ? null : new EventId(value);
  }

  @Nullable
  default EventExternalId toEventExternalId(String value) {
    return value == null ? null : new EventExternalId(value);
  }

  @Nullable
  default SecretString toSecretString(String value) {
    return value == null ? null : new SecretString(value);
  }

  @Nullable
  default ConferencingUserId toConferencingUserId(UUID value) {
    return value == null ? null : new ConferencingUserId(value);
  }
}
