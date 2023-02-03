package com.UoU.core.conferencing;

import com.UoU.core.SecretString;
import java.time.Instant;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record ConferencingUserUpdateRequest(
    @NotNull @Valid ConferencingUserId id,
    @NotBlank @Size(max = ConferencingConstraints.USER_NAME_MAX) String name,
    @NotNull @Valid SecretString refreshToken,
    @NotNull @Valid SecretString accessToken,
    @NotNull @Future Instant expireAt) {

  @lombok.Builder(builderClassName = "Builder")
  public ConferencingUserUpdateRequest {
  }
}
