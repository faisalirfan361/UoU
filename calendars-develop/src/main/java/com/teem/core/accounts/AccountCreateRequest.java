package com.UoU.core.accounts;

import com.UoU.core.OrgId;
import com.UoU.core.SecretString;
import com.UoU.core.auth.AuthMethod;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record AccountCreateRequest(
    @NotNull @Valid AccountId id,
    @Valid ServiceAccountId serviceAccountId,
    @NotNull @Valid OrgId orgId,
    @NotBlank @Size(max = AccountConstraints.NAME_MAX) String name,
    @NotEmpty @Email String email,
    @NotNull AuthMethod authMethod,
    @NotNull @Valid SecretString accessToken,
    SyncState syncState
) {

  @lombok.Builder(builderClassName = "Builder")
  public AccountCreateRequest {
  }
}
