package com.UoU.infra.db.mapping;

import static com.UoU.infra.jooq.Tables.SERVICE_ACCOUNT;

import com.UoU.core.OrgId;
import com.UoU.core.accounts.ServiceAccount;
import com.UoU.core.accounts.ServiceAccountCreateRequest;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.accounts.ServiceAccountUpdateRequest;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.mapping.CommonMapper;
import com.UoU.infra.jooq.tables.records.ServiceAccountRecord;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jooq.Record6;
import org.mapstruct.AfterMapping;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ValueMapping;

@Mapper(config = JooqConfig.class)
public interface JooqServiceAccountMapper extends CommonMapper {

  ServiceAccount toModel(ServiceAccountRecord record);

  default ServiceAccount toModel(
      Record6<UUID, String, String, com.UoU.infra.jooq.enums.AuthMethod,
          OffsetDateTime, OffsetDateTime> record) {
    return new ServiceAccount(
        new ServiceAccountId(record.value1()),
        new OrgId(record.value2()),
        record.value3(),
        mapAuthMethod(record.value4()),
        mapToInstant(record.value5()),
        mapToInstant(record.value6()));
  }

  @Mapping(target = "createdAt", expression = Expressions.NOW)
  ServiceAccountRecord toRecord(ServiceAccountCreateRequest request);

  @Mapping(target = "updatedAt", expression = Expressions.NOW)
  @Mapping(target = "orgId", ignore = true)
  ServiceAccountRecord toRecord(ServiceAccountUpdateRequest request);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "lower")
  @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = MappingConstants.ANY_REMAINING)
  com.UoU.infra.jooq.enums.AuthMethod mapAuthMethod(AuthMethod authMethod);

  @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
  AuthMethod mapAuthMethod(com.UoU.infra.jooq.enums.AuthMethod authMethod);

  @AfterMapping
  static void setFieldsForUpdate(
      @MappingTarget ServiceAccountRecord record, ServiceAccountUpdateRequest request) {

    // Ensure certain fields never change:
    record.changed(SERVICE_ACCOUNT.ID, false);
    record.changed(SERVICE_ACCOUNT.ORG_ID, false);
    record.changed(SERVICE_ACCOUNT.EMAIL, false);
    record.changed(SERVICE_ACCOUNT.AUTH_METHOD, false);
    record.changed(SERVICE_ACCOUNT.CREATED_AT, false);
  }
}
