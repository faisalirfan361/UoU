package com.UoU.core.auth;

import com.UoU.core.OrgId;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public record AuthCode(
    @NotNull UUID code,
    @NotNull OrgId orgId,
    String redirectUri
) {
}
