package com.UoU.app.v1.mapping;

import com.UoU.app.v1.dtos.AuthMethodDto;
import com.UoU.app.v1.dtos.PagedItems;
import com.UoU.app.v1.dtos.ServiceAccountDto;
import com.UoU.core.accounts.ServiceAccount;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.mapping.Config;
import com.UoU.core.mapping.WrappedValueMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

@Mapper(config = Config.class, uses = WrappedValueMapper.class)
public interface ServiceAccountMapper extends BaseMapper {
  ServiceAccountDto toDto(ServiceAccount model);

  @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = MappingConstants.ANY_REMAINING)
  AuthMethodDto.ServiceAccount toDto(AuthMethod model);

  default PagedItems<ServiceAccountDto> toDto(com.UoU.core.PagedItems<ServiceAccount> model) {
    return toPagedItemsDto(model, this::toDto);
  }
}
