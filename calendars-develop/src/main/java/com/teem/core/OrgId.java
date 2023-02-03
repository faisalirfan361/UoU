package com.UoU.core;

import javax.validation.constraints.NotBlank;
import lombok.NonNull;

public record OrgId(@NonNull @NotBlank String value) implements WrappedValue<String> {
}
