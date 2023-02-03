package com.UoU.core.accounts;

import com.UoU.core.SecretString;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record AccountUpdateRequest(
    @NotNull @Valid AccountId id,
    @NotBlank @Size(max = AccountConstraints.NAME_MAX) String name,
    @NotNull @Valid SecretString accessToken,
    SyncState syncState
) {

  @lombok.Builder(builderClassName = "Builder")
  public AccountUpdateRequest {
  }
}
