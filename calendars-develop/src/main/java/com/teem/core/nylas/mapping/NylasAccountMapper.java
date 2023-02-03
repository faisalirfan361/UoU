package com.UoU.core.nylas.mapping;

import com.nylas.Account;
import com.UoU.core.accounts.SyncState;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

@Mapper(config = NylasConfig.class)
public interface NylasAccountMapper {

  default SyncState toSyncStateModel(Account account) {
    return toSyncStateModel(account == null ? null : account.getSyncState());
  }

  /**
   * Maps string to SyncState, converting null and any unrecognized values to UNKNOWN.
   *
   * <p>This converts all bad/unknown values from Nylas to UNKNOWN because they seem to have changed
   * these statuses a couple times, and even the current docs aren't very clear about all the
   * possible values. We don't want errors for unknown values, since this is mostly just for info.
   */
  @EnumMapping(nameTransformationStrategy = "case", configuration = "lower")
  @ValueMapping(target = "INVALID_CREDENTIALS", source = "invalid-credentials") // handle - to _
  @ValueMapping(target = "SYNC_ERROR", source = "sync-error") // handle - to _
  @ValueMapping(target = "UNKNOWN", source = MappingConstants.NULL)
  @ValueMapping(target = "UNKNOWN", source = MappingConstants.ANY_REMAINING)
  SyncState toSyncStateModel(String syncState);
}
