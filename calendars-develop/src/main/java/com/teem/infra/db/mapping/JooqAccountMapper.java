package com.UoU.infra.db.mapping;

import static com.UoU.infra.jooq.Tables.ACCOUNT;
import static org.mapstruct.NullValueCheckStrategy.ALWAYS;

import com.UoU.core.OrgId;
import com.UoU.core.accounts.Account;
import com.UoU.core.accounts.AccountCreateRequest;
import com.UoU.core.accounts.AccountError;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountUpdateRequest;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.accounts.SyncState;
import com.UoU.core.auth.SubaccountAuthRequest;
import com.UoU.core.mapping.CommonMapper;
import com.UoU.infra.jooq.enums.AccountErrorType;
import com.UoU.infra.jooq.enums.AuthMethod;
import com.UoU.infra.jooq.enums.NylasAccountSyncState;
import com.UoU.infra.jooq.tables.records.AccountErrorRecord;
import com.UoU.infra.jooq.tables.records.AccountRecord;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jooq.Record5;
import org.jooq.Record9;
import org.mapstruct.AfterMapping;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ValueMapping;

@Mapper(config = JooqConfig.class)
public interface JooqAccountMapper extends CommonMapper {
  /**
   * Converts from a generic record by passing values into the Account constructor in order.
   */
  default Account toModel(
      Record9<String, UUID, String, String, String, NylasAccountSyncState,
                AuthMethod, OffsetDateTime, OffsetDateTime> record
  ) {
    return new Account(
        new AccountId(record.value1()),
        record.value2() == null ? null : new ServiceAccountId(record.value2()),
        new OrgId(record.value3()),
        record.value4(),
        record.value5(),
        toModelEnum(record.value6()),
        toModelEnum(record.value7()),
        mapToInstant(record.value8()),
        mapToInstant(record.value9()));
  }

  AccountError toModel(AccountErrorRecord record);

  default AccountError toModel(
      Record5<UUID, String, OffsetDateTime, AccountErrorType, String> record) {
    return new AccountError(
        record.value1(),
        new AccountId(record.value2()),
        mapToInstant(record.value3()),
        toModelEnum(record.value4()),
        record.value5(),
        null);
  }

  @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
  @ValueMapping(target = "UNKNOWN", source = MappingConstants.NULL)
  SyncState toModelEnum(NylasAccountSyncState syncState);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
  com.UoU.core.auth.AuthMethod toModelEnum(AuthMethod authMethod);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
  AccountError.Type toModelEnum(AccountErrorType type);

  @Mapping(target = "createdAt", expression = Expressions.NOW)
  @Mapping(target = "linkedAt", expression = Expressions.NOW)
  @Mapping(target = "nylasSyncState", source = "request.syncState")
  AccountRecord toRecord(AccountCreateRequest request, byte[] accessTokenEncrypted);

  @Mapping(target = "updatedAt", expression = Expressions.NOW)
  @Mapping(target = "linkedAt", expression = Expressions.NOW)
  @Mapping(target = "nylasSyncState", source = "request.syncState", nullValueCheckStrategy = ALWAYS)
  AccountRecord toRecord(AccountUpdateRequest request, byte[] accessTokenEncrypted);

  @Mapping(target = "createdAt", expression = Expressions.NOW)
  @Mapping(target = "linkedAt", expression = Expressions.NOW)
  AccountRecord toRecord(SubaccountAuthRequest request, AccountId id, byte[] accessTokenEncrypted);

  AccountErrorRecord toRecord(AccountError error);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "lower")
  AccountErrorType toRecordEnum(AccountError.Type type);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "lower")
  @ValueMapping(target = MappingConstants.NULL, source = "UNKNOWN")
  NylasAccountSyncState toRecordEnum(SyncState syncState);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "lower")
  @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = MappingConstants.ANY_REMAINING)
  AuthMethod toRecordEnum(com.UoU.core.auth.AuthMethod authMethod);

  @AfterMapping
  static void setFieldsForUpdate(
      @MappingTarget AccountRecord record, AccountUpdateRequest request) {

    // Ensure certain fields never change:
    record.changed(ACCOUNT.ID, false);
    record.changed(ACCOUNT.ORG_ID, false);
    record.changed(ACCOUNT.EMAIL, false);
    record.changed(ACCOUNT.AUTH_METHOD, false);
    record.changed(ACCOUNT.CREATED_AT, false);
  }
}
