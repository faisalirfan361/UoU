package com.UoU.core.accounts;

import com.UoU.core.WrappedValue;
import javax.validation.constraints.NotBlank;
import lombok.NonNull;

public record AccountId(@NonNull @NotBlank String value) implements WrappedValue<String> {
}
