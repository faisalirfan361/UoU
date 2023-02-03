package com.UoU.core.events;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public record Owner(
    String name,
    @NotEmpty @Email String email
) {
}
