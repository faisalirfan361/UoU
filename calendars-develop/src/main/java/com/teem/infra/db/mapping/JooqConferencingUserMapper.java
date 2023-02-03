package com.UoU.infra.db.mapping;

import static com.UoU.infra.jooq.Tables.CONFERENCING_USER;

import com.UoU.core.OrgId;
import com.UoU.core.conferencing.ConferencingUser;
import com.UoU.core.conferencing.ConferencingUserCreateRequest;
import com.UoU.core.conferencing.ConferencingUserId;
import com.UoU.core.conferencing.ConferencingUserUpdateRequest;
import com.UoU.core.mapping.CommonMapper;
import com.UoU.infra.jooq.enums.ConferencingAuthMethod;
import com.UoU.infra.jooq.tables.records.ConferencingUserRecord;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jooq.Record8;
import org.mapstruct.AfterMapping;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ValueMapping;

@Mapper(config = JooqConfig.class)
public interface JooqConferencingUserMapper extends CommonMapper {

  /**
   * Converts from a generic record by passing values into the model constructor in order.
   */
  default ConferencingUser toModel(Record8<
      UUID, String, String, String, ConferencingAuthMethod, OffsetDateTime, OffsetDateTime,
      OffsetDateTime> record
  ) {
    return new ConferencingUser(
        new ConferencingUserId(record.value1()),
        new OrgId(record.value2()),
        record.value3(),
        record.value4(),
        toAuthMethodModel(record.value5()),
        mapToInstant(record.value6()),
        mapToInstant(record.value7()),
        mapToInstant(record.value8()));
  }

  @Mapping(target = "createdAt", expression = Expressions.NOW)
  ConferencingUserRecord toRecord(
      ConferencingUserCreateRequest request,
      byte[] refreshTokenEncrypted,
      byte[] accessTokenEncrypted);

  @Mapping(target = "updatedAt", expression = Expressions.NOW)
  ConferencingUserRecord toRecord(
      ConferencingUserUpdateRequest request,
      byte[] refreshTokenEncrypted,
      byte[] accessTokenEncrypted);

  @AfterMapping
  static void setFieldsForUpdate(
      @MappingTarget ConferencingUserRecord record, ConferencingUserUpdateRequest request) {

    // Ensure certain fields never change:
    record.changed(CONFERENCING_USER.ID, false);
    record.changed(CONFERENCING_USER.ORG_ID, false);
    record.changed(CONFERENCING_USER.EMAIL, false);
    record.changed(CONFERENCING_USER.CREATED_AT, false);
  }

  @EnumMapping(nameTransformationStrategy = "case", configuration = "lower")
  @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = MappingConstants.ANY_REMAINING)
  ConferencingAuthMethod toAuthMethodRecord(com.UoU.core.auth.AuthMethod authMethod);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
  com.UoU.core.auth.AuthMethod toAuthMethodModel(ConferencingAuthMethod authMethod);
}
