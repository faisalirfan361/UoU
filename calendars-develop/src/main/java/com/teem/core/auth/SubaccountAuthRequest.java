package com.UoU.core.auth;

import com.UoU.core.OrgId;
import com.UoU.core.accounts.AccountConstraints;
import com.UoU.core.accounts.ServiceAccountId;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Request to auth a service account subaccount and save the resulting Account locally.
 */
public record SubaccountAuthRequest(
    @NotNull @Valid ServiceAccountId serviceAccountId,
    @NotNull @Valid OrgId orgId,
    @NotBlank @Size(max = AccountConstraints.NAME_MAX) String name,
    @NotEmpty @Email String email
) {
}
