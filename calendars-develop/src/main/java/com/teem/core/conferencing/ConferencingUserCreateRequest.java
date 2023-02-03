package com.UoU.core.conferencing;

import com.UoU.core.OrgId;
import com.UoU.core.SecretString;
import com.UoU.core.auth.AuthMethod;
import java.time.Instant;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record ConferencingUserCreateRequest(
    @NotNull @Valid ConferencingUserId id,
    @NotNull @Valid OrgId orgId,
    @NotBlank @Size(max = ConferencingConstraints.USER_NAME_MAX) String name,
    @NotEmpty @Email String email,
    @NotNull AuthMethod authMethod,
    @NotNull @Valid SecretString refreshToken,
    @NotNull @Valid SecretString accessToken,
    @NotNull @Future Instant expireAt) {

  @lombok.Builder(builderClassName = "Builder")
  public ConferencingUserCreateRequest {
  }
}
