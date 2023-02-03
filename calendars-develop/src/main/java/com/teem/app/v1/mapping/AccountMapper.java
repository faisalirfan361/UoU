package com.UoU.app.v1.mapping;

import com.UoU.app.v1.dtos.AccountDto;
import com.UoU.app.v1.dtos.AccountErrorDto;
import com.UoU.app.v1.dtos.AuthMethodDto;
import com.UoU.app.v1.dtos.PagedItems;
import com.UoU.core.accounts.Account;
import com.UoU.core.accounts.AccountError;
import com.UoU.core.accounts.SyncState;
import com.UoU.core.auth.AuthMethod;
import com.UoU.core.mapping.Config;
import com.UoU.core.mapping.WrappedValueMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

@Mapper(config = Config.class, uses = WrappedValueMapper.class)
public interface AccountMapper extends BaseMapper {
  AccountDto toDto(Account model);

  @ValueMapping(target = MappingConstants.THROW_EXCEPTION, source = MappingConstants.ANY_REMAINING)
  AuthMethodDto.Account toDto(AuthMethod model);

  @ValueMapping(target = "UNKNOWN", source = MappingConstants.NULL)
  AccountDto.SyncStateDto toDto(SyncState syncState);

  default PagedItems<AccountDto> toDto(com.UoU.core.PagedItems<Account> model) {
    return toPagedItemsDto(model, this::toDto);
  }

  AccountErrorDto toDto(AccountError model);
}
