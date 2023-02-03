package com.UoU.core.auth;

import com.UoU.core.OrgId;
import java.time.Duration;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.time.DurationMin;

public record AuthCodeCreateRequest(
    @NotNull
    UUID code,

    @NotNull @Valid
    OrgId orgId,

    @NotNull
    @DurationMin(minutes = AuthConstraints.AUTH_CODE_MIN_DURATION_MINUTES)
    Duration expiration,

    @Pattern(regexp = "^https?://.+")
    @Size(max = AuthConstraints.REDIRECT_URI_MAX)
    String redirectUri) {
}
