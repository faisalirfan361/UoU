package com.UoU.app.v1.mapping;

import com.UoU.app.v1.dtos.AuthCodeCreateRequestDto;
import com.UoU.app.v1.dtos.SubaccountCreateRequestDto;
import com.UoU.core.OrgId;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.auth.AuthCodeCreateRequest;
import com.UoU.core.auth.SubaccountAuthRequest;
import com.UoU.core.mapping.Config;
import com.UoU.core.mapping.WrappedValueMapper;
import java.time.Duration;
import java.util.UUID;
import org.mapstruct.Mapper;

@Mapper(config = Config.class, uses = WrappedValueMapper.class)
public interface AuthMapper {
  default AuthCodeCreateRequest toModel(
      AuthCodeCreateRequestDto dto, OrgId orgId, int expirationMinutes) {
    return new AuthCodeCreateRequest(
        UUID.randomUUID(),
        orgId,
        Duration.ofMinutes(expirationMinutes),
        dto == null ? null : dto.redirectUri());
  }

  SubaccountAuthRequest toModel(
      SubaccountCreateRequestDto dto, ServiceAccountId serviceAccountId, OrgId orgId);
}
